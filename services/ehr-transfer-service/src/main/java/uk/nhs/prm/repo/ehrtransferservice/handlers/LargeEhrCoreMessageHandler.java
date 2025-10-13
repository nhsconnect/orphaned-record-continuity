package uk.nhs.prm.repo.ehrtransferservice.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.nhs.prm.repo.ehrtransferservice.database.TransferService;
import uk.nhs.prm.repo.ehrtransferservice.database.model.ConversationRecord;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.ParsedMessage;
import uk.nhs.prm.repo.ehrtransferservice.services.AuditService;
import uk.nhs.prm.repo.ehrtransferservice.services.ehr_repo.EhrRepoService;
import uk.nhs.prm.repo.ehrtransferservice.services.gp2gp_messenger.Gp2gpMessengerService;

import java.util.UUID;

import static uk.nhs.prm.repo.ehrtransferservice.database.enumeration.ConversationTransferStatus.INBOUND_CONTINUE_REQUEST_SENT;

@Slf4j
@Service
@RequiredArgsConstructor
public class LargeEhrCoreMessageHandler implements MessageHandler<ParsedMessage> {
    private final EhrRepoService ehrRepoService;
    private final Gp2gpMessengerService gp2gpMessengerService;
    private final TransferService transferService;
    private final AuditService auditService;

    @Override
    public void handleMessage(ParsedMessage largeEhrCoreMessage) throws Exception {
        final UUID conversationId = largeEhrCoreMessage.getConversationId();
        ehrRepoService.storeMessage(largeEhrCoreMessage);

        log.info("Successfully stored EHR Core for Inbound Conversation ID {}",
            largeEhrCoreMessage.getConversationId());

        final ConversationRecord conversation = transferService
            .getConversationByInboundConversationId(conversationId);

        gp2gpMessengerService.sendContinueMessage(largeEhrCoreMessage, conversation.sourceGp());
        transferService.updateConversationTransferStatus(conversationId, INBOUND_CONTINUE_REQUEST_SENT);
        auditService.publishAuditMessage(conversationId, INBOUND_CONTINUE_REQUEST_SENT, conversation.nemsMessageId());
    }
}