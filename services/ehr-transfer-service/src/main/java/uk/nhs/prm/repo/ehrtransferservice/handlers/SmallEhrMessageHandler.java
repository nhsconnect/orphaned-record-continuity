package uk.nhs.prm.repo.ehrtransferservice.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.ParsedMessage;
import uk.nhs.prm.repo.ehrtransferservice.services.ConversationActivityService;
import uk.nhs.prm.repo.ehrtransferservice.services.ehr_repo.EhrRepoService;
import uk.nhs.prm.repo.ehrtransferservice.services.ehr_repo.StoreMessageResult;
import uk.nhs.prm.repo.ehrtransferservice.services.gp2gp_messenger.Gp2gpMessengerService;

import static uk.nhs.prm.repo.ehrtransferservice.utility.UuidUtility.getUuidAsUpperCasedStringIfNotNull;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmallEhrMessageHandler implements MessageHandler<ParsedMessage> {
    private final EhrRepoService ehrRepoService;
    private final Gp2gpMessengerService gp2gpMessengerService;
    private final ConversationActivityService conversationActivityService;

    @Override
    public void handleMessage(ParsedMessage parsedMessage) throws Exception {
        final StoreMessageResult storeMessageResult = ehrRepoService.storeMessage(parsedMessage);

        if (storeMessageResult.isEhrComplete()) {
            log.info("The Small EHR with Inbound Conversation ID {} has been stored successfully",
                    getUuidAsUpperCasedStringIfNotNull(parsedMessage.getConversationId()));

            conversationActivityService.concludeConversationActivity(parsedMessage.getConversationId());

            gp2gpMessengerService.sendEhrCompletePositiveAcknowledgement(parsedMessage.getConversationId());
        }
    }
}