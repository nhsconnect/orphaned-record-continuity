package uk.nhs.prm.repo.ehrtransferservice.handlers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.repo.ehrtransferservice.builders.ConversationRecordBuilder;
import uk.nhs.prm.repo.ehrtransferservice.database.TransferService;
import uk.nhs.prm.repo.ehrtransferservice.database.enumeration.ConversationTransferStatus;
import uk.nhs.prm.repo.ehrtransferservice.database.model.ConversationRecord;
import uk.nhs.prm.repo.ehrtransferservice.models.LargeSqsMessage;
import uk.nhs.prm.repo.ehrtransferservice.services.AuditService;
import uk.nhs.prm.repo.ehrtransferservice.services.ehr_repo.EhrRepoService;
import uk.nhs.prm.repo.ehrtransferservice.services.gp2gp_messenger.Gp2gpMessengerService;

import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LargeEhrCoreMessageHandlerTest {
    @Mock
    LargeSqsMessage largeSqsMessage;

    @Mock
    EhrRepoService ehrRepoService;

    @Mock
    Gp2gpMessengerService gp2gpMessengerService;

    @Mock
    TransferService transferService;

    @Mock
    AuditService auditService;

    @InjectMocks
    LargeEhrCoreMessageHandler largeEhrCoreMessageHandler;

    private static final ConversationRecord CONVERSATION_RECORD;

    private static final UUID INBOUND_CONVERSATION_ID = UUID.fromString("4c77d6c9-00cd-47b6-8974-e66bdd2697e2");

    static {
        final ConversationRecordBuilder builder = new ConversationRecordBuilder();
        CONVERSATION_RECORD = builder.withDefaults()
            .withInboundConversationId(INBOUND_CONVERSATION_ID)
            .build();
    }

    @Test
    void shouldCallEhrRepoServiceAndGp2gpMessengerServiceAndTransferServiceToHandleLargeEhrCore() throws Exception {
        // when
        when(transferService.getConversationByInboundConversationId(INBOUND_CONVERSATION_ID))
            .thenReturn(CONVERSATION_RECORD);
        when(largeSqsMessage.getConversationId())
            .thenReturn(INBOUND_CONVERSATION_ID);

        largeEhrCoreMessageHandler.handleMessage(largeSqsMessage);

        // then
        verify(ehrRepoService).storeMessage(largeSqsMessage);
        verify(gp2gpMessengerService).sendContinueMessage(largeSqsMessage, CONVERSATION_RECORD.sourceGp());
        verify(transferService).updateConversationTransferStatus(INBOUND_CONVERSATION_ID,
            ConversationTransferStatus.INBOUND_CONTINUE_REQUEST_SENT);
        verify(auditService).publishAuditMessage(INBOUND_CONVERSATION_ID, ConversationTransferStatus.INBOUND_CONTINUE_REQUEST_SENT, CONVERSATION_RECORD.nemsMessageId());
    }
}
