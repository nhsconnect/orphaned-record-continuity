package uk.nhs.prm.repo.ehrtransferservice.database;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.repo.ehrtransferservice.builders.ConversationRecordBuilder;
import uk.nhs.prm.repo.ehrtransferservice.database.enumeration.ConversationTransferStatus;
import uk.nhs.prm.repo.ehrtransferservice.database.model.ConversationRecord;
import uk.nhs.prm.repo.ehrtransferservice.exceptions.ConversationIneligibleForRetryException;
import uk.nhs.prm.repo.ehrtransferservice.repo_incoming.RepoIncomingEvent;
import uk.nhs.prm.repo.ehrtransferservice.services.ConversationActivityService;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.nhs.prm.repo.ehrtransferservice.database.enumeration.ConversationTransferStatus.*;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {
    @Mock
    private TransferRepository transferRepository;

    @Mock
    private ConversationActivityService activityService;

    @InjectMocks
    private TransferService transferService;

    private final ConversationRecordBuilder conversationRecordBuilder = new ConversationRecordBuilder();

    private static final String NHS_NUMBER = "9798547485";
    private static final String SOURCE_GP = "B45744";
    private static final String NEMS_MESSAGE_ID = "2d74a113-1076-4c63-91bc-e50d232b6a79";

    @Test
    void createExceptionOrResetForRetry_ValidNewConversationOrResetForRetryRequest_Ok() {
        // given
        final UUID inboundConversationId = UUID.randomUUID();
        final RepoIncomingEvent repoIncomingEvent = createRepoIncomingEvent(inboundConversationId);

        // when
        when(transferRepository.isInboundConversationPresent(inboundConversationId)).thenReturn(false);

        // then
        assertDoesNotThrow(() -> transferService.createConversationOrResetForRetry(repoIncomingEvent));

        verify(transferRepository).isInboundConversationPresent(inboundConversationId);
        verify(activityService).captureConversationActivity(inboundConversationId);
        verify(transferRepository).createConversation(repoIncomingEvent);

        verify(transferRepository, never()).updateConversationStatus(any(), any());
    }

    @Test
    void createExceptionOrResetForRetry_ValidRetriedConversationOrResetForRetryRequest_Ok() {
        // given
        final UUID inboundConversationId = UUID.randomUUID();
        final RepoIncomingEvent repoIncomingEvent = createRepoIncomingEvent(inboundConversationId);
        final ConversationRecord conversationRecord = createConversationRecord(inboundConversationId, INBOUND_REQUEST_SENT);

        // when
        when(transferRepository.isInboundConversationPresent(inboundConversationId)).thenReturn(true);
        when(transferRepository.findConversationByInboundConversationId(inboundConversationId)).thenReturn(conversationRecord);

        // then
        assertDoesNotThrow(() -> transferService.createConversationOrResetForRetry(repoIncomingEvent));

        verify(transferRepository).isInboundConversationPresent(inboundConversationId);
        verify(transferRepository).findConversationByInboundConversationId(inboundConversationId);
        verify(activityService).captureConversationActivity(inboundConversationId);
        verify(transferRepository).updateConversationStatus(inboundConversationId, INBOUND_STARTED);

        verify(transferRepository, never()).createConversation(any());
    }

    @Test
    void createExceptionOrResetForRetry_RetriedConversationRequestNotRetryable_ThrowsConversationIneligibleForRetryException() {
        // given
        final UUID inboundConversationId = UUID.randomUUID();
        final RepoIncomingEvent repoIncomingEvent = createRepoIncomingEvent(inboundConversationId);
        final ConversationRecord conversationRecord = createConversationRecord(inboundConversationId, INBOUND_FAILED);

        // when
        when(transferRepository.isInboundConversationPresent(inboundConversationId)).thenReturn(true);
        when(transferRepository.findConversationByInboundConversationId(inboundConversationId)).thenReturn(conversationRecord);

        // then
        assertThrows(ConversationIneligibleForRetryException.class, () -> transferService.createConversationOrResetForRetry(repoIncomingEvent));

        verify(transferRepository).isInboundConversationPresent(inboundConversationId);
        verify(transferRepository).findConversationByInboundConversationId(inboundConversationId);

        verify(activityService, never()).captureConversationActivity(any());
        verify(transferRepository, never()).updateConversationStatus(any(), any());
        verify(transferRepository, never()).createConversation(any());
    }

    @Test
    void updateConversationTransferStatus_ConversationIsAlreadyComplete_DoNotUpdateTransferStatusAndConcludeConversation() {
        // given
        final UUID inboundConversationId = UUID.randomUUID();
        final ConversationTransferStatus currentTransferStatus = INBOUND_COMPLETE;
        final ConversationTransferStatus newTransferStatus = INBOUND_TIMEOUT;
        final ConversationRecord conversationRecord = createConversationRecord(inboundConversationId, currentTransferStatus);

        // when
        when(transferRepository.findConversationByInboundConversationId(inboundConversationId))
                .thenReturn(conversationRecord);

        // this should only result in a warn log message, nothing else can be checked
        transferService.updateConversationTransferStatus(inboundConversationId, newTransferStatus);

        // then
        verify(transferRepository, never()).updateConversationStatus(any(), any());
        verify(activityService).concludeConversationActivity(inboundConversationId);
    }

    @Test
    void updateConversationTransferStatus_ConversationIsPendingAndNewStatusIsPending_UpdateTransferStatusAndDoNotConcludeConversation() {
        // given
        final UUID inboundConversationId = UUID.randomUUID();
        final ConversationTransferStatus currentTransferStatus = INBOUND_STARTED;
        final ConversationTransferStatus newTransferStatus = INBOUND_REQUEST_SENT;
        final ConversationRecord conversationRecord = createConversationRecord(inboundConversationId, currentTransferStatus);

        // when
        when(transferRepository.findConversationByInboundConversationId(inboundConversationId))
                .thenReturn(conversationRecord);

        transferService.updateConversationTransferStatus(inboundConversationId, newTransferStatus);

        // then
        verify(transferRepository).updateConversationStatus(inboundConversationId, newTransferStatus);
        verify(activityService, never()).concludeConversationActivity(any());
    }

    @Test
    void updateConversationTransferStatus_ConversationIsPendingAndNewStatusIsTerminating_UpdateTransferStatusAndConcludeConversation() {
        // given
        final UUID inboundConversationId = UUID.randomUUID();
        final ConversationTransferStatus currentTransferStatus = INBOUND_REQUEST_SENT;
        final ConversationTransferStatus newTransferStatus = INBOUND_COMPLETE;
        final ConversationRecord conversationRecord = createConversationRecord(inboundConversationId, currentTransferStatus);

        // when
        when(transferRepository.findConversationByInboundConversationId(inboundConversationId))
                .thenReturn(conversationRecord);

        transferService.updateConversationTransferStatus(inboundConversationId, newTransferStatus);

        // then
        verify(transferRepository).updateConversationStatus(inboundConversationId, newTransferStatus);
        verify(activityService).concludeConversationActivity(inboundConversationId);
    }

    @Test
    void updateConversationTransferStatusWithFailure_ConversationIsAlreadyComplete_DoNotUpdateTransferStatusToFailedAndConcludeConversation() {
        // given
        final UUID inboundConversationId = UUID.randomUUID();
        final ConversationTransferStatus currentTransferStatus = INBOUND_COMPLETE;
        final String failureCode = "06";
        final ConversationRecord conversationRecord = createConversationRecord(inboundConversationId, currentTransferStatus);

        // when
        when(transferRepository.findConversationByInboundConversationId(inboundConversationId))
                .thenReturn(conversationRecord);

        transferService.updateConversationTransferStatusWithFailure(inboundConversationId, failureCode);

        // then
        verify(transferRepository, never()).updateConversationStatusWithFailure(any(), any());
        verify(activityService).concludeConversationActivity(inboundConversationId);
    }

    @Test
    void updateConversationTransferStatusWithFailure_ConversationIsPending_UpdateTransferStatusToFailedAndConcludeConversation() {
        // given
        final UUID inboundConversationId = UUID.randomUUID();
        final ConversationTransferStatus currentTransferStatus = INBOUND_REQUEST_SENT;
        final String failureCode = "06";
        final ConversationRecord conversationRecord = createConversationRecord(inboundConversationId, currentTransferStatus);

        // when
        when(transferRepository.findConversationByInboundConversationId(inboundConversationId))
                .thenReturn(conversationRecord);

        transferService.updateConversationTransferStatusWithFailure(inboundConversationId, failureCode);

        // then
        verify(transferRepository).updateConversationStatusWithFailure(inboundConversationId, failureCode);
        verify(activityService).concludeConversationActivity(inboundConversationId);
    }

    // Helper Methods
    private ConversationRecord createConversationRecord(
            UUID inboundConversationId,
            ConversationTransferStatus currentTransferStatus
    ) {
        return conversationRecordBuilder
                .withDefaults()
                .withInboundConversationId(inboundConversationId)
                .withTransferStatus(currentTransferStatus)
                .build();
    }

    private RepoIncomingEvent createRepoIncomingEvent(UUID inboundConversationId) {
        return RepoIncomingEvent.builder()
                .nhsNumber(NHS_NUMBER)
                .sourceGp(SOURCE_GP)
                .nemsMessageId(NEMS_MESSAGE_ID)
                .conversationId(inboundConversationId.toString().toUpperCase())
                .build();
    }
}