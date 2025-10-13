package uk.nhs.prm.repo.ehrtransferservice.handlers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.repo.ehrtransferservice.database.TransferService;
import uk.nhs.prm.repo.ehrtransferservice.models.ack.Acknowledgement;
import uk.nhs.prm.repo.ehrtransferservice.models.ack.FailureDetail;
import uk.nhs.prm.repo.ehrtransferservice.models.ack.FailureLevel;
import uk.nhs.prm.repo.ehrtransferservice.services.AuditService;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.nhs.prm.repo.ehrtransferservice.database.enumeration.ConversationTransferStatus.INBOUND_FAILED;

@ExtendWith(MockitoExtension.class)
class NegativeAcknowledgementHandlerTest {
    @Mock
    private TransferService transferService;
    @Mock
    private AuditService auditService;
    @Mock
    private Acknowledgement acknowledgement;
    @InjectMocks
    private NegativeAcknowledgementHandler negativeAcknowledgementHandler;

    private static final UUID INBOUND_CONVERSATION_ID = UUID.fromString("d263e1ed-95d5-4502-95f8-905c10edc85f");
    private static final UUID NEMS_MESSAGE_ID = UUID.fromString("8dd4f4be-9449-4efb-b231-e574415dbf86");
    private static final String DEFAULT_FAILURE_CODE = "UNKNOWN_ERROR";
    private static final String FAILURE_CODE = "19";
    private static final List<FailureDetail> FAILURE_DETAILS;

    static {
        FAILURE_DETAILS = List.of(
            new FailureDetail("DNO", FAILURE_CODE, "CSO", FailureLevel.ERROR),
            new FailureDetail("DNT", "09", "CST", FailureLevel.INFO)
        );
    }

    @Test
    void handleMessage_ValidAcknowledgement_UpdatesTransferStatusWithFirstFailureDetailAndPublishesAuditMessage() {
        // when
        when(acknowledgement.getConversationId())
            .thenReturn(INBOUND_CONVERSATION_ID);
        when(transferService.getNemsMessageIdAsUuid(INBOUND_CONVERSATION_ID))
            .thenReturn(Optional.of(NEMS_MESSAGE_ID));
        when(acknowledgement.getFailureDetails())
            .thenReturn(FAILURE_DETAILS);

        negativeAcknowledgementHandler.handleMessage(acknowledgement);

        // then
        verify(transferService).updateConversationTransferStatusWithFailure(INBOUND_CONVERSATION_ID, FAILURE_CODE);
        verify(auditService).publishAuditMessage(INBOUND_CONVERSATION_ID, INBOUND_FAILED, Optional.of(NEMS_MESSAGE_ID));
    }

    @Test
    void handleMessage_ValidAcknowledgementAndNoNemsMessageId_UpdatesTransferStatusAndPublishesAuditMessage() {
        // given
        final Optional<UUID> nemsMessageId = Optional.empty();

        // when
        when(acknowledgement.getConversationId())
            .thenReturn(INBOUND_CONVERSATION_ID);
        when(transferService.getNemsMessageIdAsUuid(INBOUND_CONVERSATION_ID))
            .thenReturn(nemsMessageId);
        when(acknowledgement.getFailureDetails())
            .thenReturn(FAILURE_DETAILS);

        negativeAcknowledgementHandler.handleMessage(acknowledgement);

        // then
        verify(transferService).updateConversationTransferStatusWithFailure(INBOUND_CONVERSATION_ID, FAILURE_CODE);
        verify(auditService).publishAuditMessage(INBOUND_CONVERSATION_ID, INBOUND_FAILED, nemsMessageId);
    }

    @Test
    void handleMessage_ValidAcknowledgementWithNoFailureDetails_UpdatesTransferStatusWithUnknownErrorAndPublishesAuditMessage() {
        // when
        when(acknowledgement.getConversationId())
            .thenReturn(INBOUND_CONVERSATION_ID);
        when(transferService.getNemsMessageIdAsUuid(INBOUND_CONVERSATION_ID))
            .thenReturn(Optional.of(NEMS_MESSAGE_ID));
        when(acknowledgement.getFailureDetails())
            .thenReturn(Collections.emptyList());

        negativeAcknowledgementHandler.handleMessage(acknowledgement);

        // then
        verify(transferService).updateConversationTransferStatusWithFailure(INBOUND_CONVERSATION_ID, DEFAULT_FAILURE_CODE);
        verify(auditService).publishAuditMessage(INBOUND_CONVERSATION_ID, INBOUND_FAILED, Optional.of(NEMS_MESSAGE_ID));
    }
}