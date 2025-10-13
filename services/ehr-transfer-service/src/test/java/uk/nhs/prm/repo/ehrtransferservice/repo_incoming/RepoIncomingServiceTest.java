package uk.nhs.prm.repo.ehrtransferservice.repo_incoming;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.TestPropertySource;
import uk.nhs.prm.repo.ehrtransferservice.database.TransferService;
import uk.nhs.prm.repo.ehrtransferservice.exceptions.ConversationAlreadyInProgressException;
import uk.nhs.prm.repo.ehrtransferservice.exceptions.ConversationIneligibleForRetryException;
import uk.nhs.prm.repo.ehrtransferservice.exceptions.timeout.TimeoutExceededException;
import uk.nhs.prm.repo.ehrtransferservice.services.AuditService;
import uk.nhs.prm.repo.ehrtransferservice.services.ConversationActivityService;
import uk.nhs.prm.repo.ehrtransferservice.services.gp2gp_messenger.Gp2gpMessengerService;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static uk.nhs.prm.repo.ehrtransferservice.database.enumeration.ConversationTransferStatus.INBOUND_REQUEST_SENT;
import static uk.nhs.prm.repo.ehrtransferservice.database.enumeration.ConversationTransferStatus.INBOUND_TIMEOUT;

@TestPropertySource(locations = "classpath:application.properties")
@ExtendWith(MockitoExtension.class)
class RepoIncomingServiceTest {
    @Mock
    private TransferService transferService;
    @Mock
    private Gp2gpMessengerService gp2gpMessengerService;
    @Mock
    private AuditService auditService;
    @Mock
    private RepoIncomingEvent repoIncomingEvent;
    @Mock
    private ConversationActivityService activityService;
    @InjectMocks
    private RepoIncomingService repoIncomingService;

    private static final UUID INBOUND_CONVERSATION_ID = UUID.fromString("bbd98cce-0a86-44ee-8f51-5267d4d81303");
    private static final UUID NEMS_MESSAGE_ID = UUID.fromString("ce919f71-b1f8-4f4a-82d4-36d75daef1cb");

    @Test
    void processIncomingEvent_ValidRepoIncomingEvent_Ok() throws Exception {
        // when
        when(repoIncomingEvent.getConversationId()).thenReturn(INBOUND_CONVERSATION_ID.toString());
        when(repoIncomingEvent.getNemsMessageId()).thenReturn(NEMS_MESSAGE_ID.toString());
        when(activityService.isConversationActive(INBOUND_CONVERSATION_ID))
                .thenReturn(false) // called by verifyIfConversationAlreadyInProgress - it should not be
                // called by waitForConversationToComplete in a loop
                .thenReturn(true) // first loop - let's assume it is active and the check timeout logic doesn't kick in
                .thenReturn(false); // second loop - let's assume it has succeeded and is no longer active

        // then
        assertDoesNotThrow(() -> repoIncomingService.processIncomingEvent(repoIncomingEvent));

        verify(activityService, times(3)).isConversationActive(INBOUND_CONVERSATION_ID);
        verify(transferService).createConversationOrResetForRetry(repoIncomingEvent);
        verify(gp2gpMessengerService).sendEhrRequest(repoIncomingEvent);
        verify(transferService).updateConversationTransferStatus(INBOUND_CONVERSATION_ID, INBOUND_REQUEST_SENT);
        verify(auditService).publishAuditMessage(INBOUND_CONVERSATION_ID, INBOUND_REQUEST_SENT, Optional.of(NEMS_MESSAGE_ID));
    }

    @Test
    void processIncomingEvent_ConversationIsActiveAndIsTimedOut_ShouldConcludePreviousConversationAndProcessAsNormal() throws Exception {
        // when
        when(repoIncomingEvent.getConversationId()).thenReturn(INBOUND_CONVERSATION_ID.toString());
        when(repoIncomingEvent.getNemsMessageId()).thenReturn(NEMS_MESSAGE_ID.toString());
        when(activityService.isConversationActive(INBOUND_CONVERSATION_ID))
                .thenReturn(true) // called by verifyIfConversationAlreadyInProgress - mock an old conversation hanging around so we can conclude it
                // then these are part of the new conversation, called by waitForConversationToComplete in a loop
                .thenReturn(true) // first loop - let's assume new conversation IS active and check the timeout logic doesn't kick in
                .thenReturn(false); // second loop - let's assume new conversation has succeeded and is no longer active
        when(activityService.isConversationTimedOut(INBOUND_CONVERSATION_ID))
                .thenReturn(true) // called by verifyIfConversationAlreadyInProgress - old conversation should be timed out
                .thenReturn(false); // called by verifyConversationNotTimedOut - new conversation should not timeout

        repoIncomingService.processIncomingEvent(repoIncomingEvent);

        // then
        verify(activityService).concludeConversationActivity(INBOUND_CONVERSATION_ID); // check we conclude the old conversation before proceeding

        verify(activityService, times(3)).isConversationActive(INBOUND_CONVERSATION_ID);
        verify(activityService, times(2)).isConversationTimedOut(INBOUND_CONVERSATION_ID);
        verify(transferService).createConversationOrResetForRetry(repoIncomingEvent);
        verify(gp2gpMessengerService).sendEhrRequest(repoIncomingEvent);
        verify(transferService).updateConversationTransferStatus(INBOUND_CONVERSATION_ID, INBOUND_REQUEST_SENT);
        verify(auditService).publishAuditMessage(INBOUND_CONVERSATION_ID, INBOUND_REQUEST_SENT, Optional.of(NEMS_MESSAGE_ID));
    }

    @Test
    void processIncomingEvent_ConversationIsActiveAndIsNotTimedOut_ShouldThrowAlreadyInProgressException() throws Exception {
        // when
        when(repoIncomingEvent.getConversationId()).thenReturn(INBOUND_CONVERSATION_ID.toString());
        when(repoIncomingEvent.getNemsMessageId()).thenReturn(NEMS_MESSAGE_ID.toString());
        when(activityService.isConversationActive(INBOUND_CONVERSATION_ID)).thenReturn(true);
        when(activityService.isConversationTimedOut(INBOUND_CONVERSATION_ID)).thenReturn(false);

        // then
        assertThrows(ConversationAlreadyInProgressException.class, () -> repoIncomingService.processIncomingEvent(repoIncomingEvent));

        verify(activityService).isConversationActive(INBOUND_CONVERSATION_ID);
        verify(activityService).isConversationTimedOut(INBOUND_CONVERSATION_ID);
        
        verify(activityService, never()).concludeConversationActivity(any());
        verify(transferService, never()).createConversationOrResetForRetry(any());
        verify(gp2gpMessengerService, never()).sendEhrRequest(any());
        verify(transferService, never()).updateConversationTransferStatus(any(), any());
        verify(auditService, never()).publishAuditMessage(any(), any(), any());
    }

    @Test
    void processIncomingEvent_CreateOrRetryConversationThrowsException_ShouldNotProceedAndShouldConcludeConversation() throws Exception {
        // given
        final Exception exception = new ConversationIneligibleForRetryException(INBOUND_CONVERSATION_ID);

        // when
        when(repoIncomingEvent.getConversationId()).thenReturn(INBOUND_CONVERSATION_ID.toString());
        when(repoIncomingEvent.getNemsMessageId()).thenReturn(NEMS_MESSAGE_ID.toString());
        doThrow(exception)
                .when(transferService)
                .createConversationOrResetForRetry(repoIncomingEvent);

        // then
        assertThrows(Exception.class, () -> repoIncomingService.processIncomingEvent(repoIncomingEvent));

        verify(transferService).createConversationOrResetForRetry(repoIncomingEvent);
        verify(activityService).concludeConversationActivity(INBOUND_CONVERSATION_ID);

        verify(gp2gpMessengerService, never()).sendEhrRequest(any());
        verify(transferService, never()).updateConversationTransferStatus(any(), any());
        verify(auditService, never()).publishAuditMessage(any(), any(), any());
    }

    @Test
    void processIncomingEvent_SendEhrRequestThrowsException_ShouldNotProceedAndShouldConcludeConversation() throws Exception {
        // given
        final Exception exception = new Exception();

        // when
        when(repoIncomingEvent.getConversationId()).thenReturn(INBOUND_CONVERSATION_ID.toString());
        when(repoIncomingEvent.getNemsMessageId()).thenReturn(NEMS_MESSAGE_ID.toString());
        doThrow(exception)
            .when(gp2gpMessengerService)
            .sendEhrRequest(repoIncomingEvent);

        // then
        assertThrows(Exception.class, () -> repoIncomingService.processIncomingEvent(repoIncomingEvent));

        verify(transferService).createConversationOrResetForRetry(repoIncomingEvent);
        verify(gp2gpMessengerService).sendEhrRequest(repoIncomingEvent);
        verify(activityService).concludeConversationActivity(INBOUND_CONVERSATION_ID);

        verify(transferService, never()).updateConversationTransferStatus(any(), any());
        verify(auditService, never()).publishAuditMessage(any(), any(), any());
    }

    @Test
    void processIncomingEvent_TransferStatusDoesNotUpdate_TransferStatusUpdatesToInboundTimeoutAndThrowsTimeoutExceededException() throws Exception {
        // when
        configurePollPeriod(5000);

        when(repoIncomingEvent.getConversationId()).thenReturn(INBOUND_CONVERSATION_ID.toString());
        when(repoIncomingEvent.getNemsMessageId()).thenReturn(NEMS_MESSAGE_ID.toString());
        when(activityService.isConversationActive(INBOUND_CONVERSATION_ID))
                .thenReturn(false) // called by verifyIfConversationAlreadyInProgress - it should not be
                .thenReturn(true); // called by waitForConversationToComplete - it should still be
        when(activityService.isConversationTimedOut(INBOUND_CONVERSATION_ID)).thenReturn(true);
        when(transferService.getConversationTransferStatus(INBOUND_CONVERSATION_ID)).thenReturn(INBOUND_REQUEST_SENT);

        // then
        assertThrows(TimeoutExceededException.class, () -> repoIncomingService.processIncomingEvent(repoIncomingEvent));

        verify(transferService).createConversationOrResetForRetry(repoIncomingEvent);
        verify(gp2gpMessengerService).sendEhrRequest(repoIncomingEvent);
        verify(transferService).updateConversationTransferStatus(INBOUND_CONVERSATION_ID, INBOUND_REQUEST_SENT);
        verify(auditService).publishAuditMessage(INBOUND_CONVERSATION_ID, INBOUND_REQUEST_SENT, Optional.of(NEMS_MESSAGE_ID));
        verify(transferService).updateConversationTransferStatus(INBOUND_CONVERSATION_ID, INBOUND_TIMEOUT);
        verify(activityService).concludeConversationActivity(INBOUND_CONVERSATION_ID);
    }

    private void configurePollPeriod(int pollPeriodMilliseconds) throws NoSuchFieldException, IllegalAccessException {
        final Field pollPeriodMillisecondsField = RepoIncomingService.class.getDeclaredField("pollPeriodMilliseconds");
        pollPeriodMillisecondsField.setAccessible(true);
        pollPeriodMillisecondsField.set(this.repoIncomingService, pollPeriodMilliseconds);
    }
}