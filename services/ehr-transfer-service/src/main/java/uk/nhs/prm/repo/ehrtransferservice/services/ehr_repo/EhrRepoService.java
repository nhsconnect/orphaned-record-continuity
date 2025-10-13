package uk.nhs.prm.repo.ehrtransferservice.services.ehr_repo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.nhs.prm.repo.ehrtransferservice.exceptions.DuplicateMessageException;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.ParsedMessage;
import uk.nhs.prm.repo.ehrtransferservice.services.ConversationActivityService;
import uk.nhs.prm.repo.ehrtransferservice.services.PresignedUrl;
import uk.nhs.prm.repo.ehrtransferservice.services.gp2gp_messenger.Gp2gpMessengerService;

import java.util.UUID;

import static uk.nhs.prm.repo.ehrtransferservice.models.enums.AcknowledgementErrorCode.ERROR_CODE_12;

@Service
@Slf4j
public class EhrRepoService {
    private final EhrRepoClient ehrRepoClient;
    private final ConversationActivityService activityService;
    private final Gp2gpMessengerService gp2gpMessengerService;

    public EhrRepoService(
            EhrRepoClient ehrRepoClient,
            ConversationActivityService conversationActivityService,
            Gp2gpMessengerService gp2gpMessengerService
    ) {
        this.ehrRepoClient = ehrRepoClient;
        this.activityService = conversationActivityService;
        this.gp2gpMessengerService = gp2gpMessengerService;
    }

    public StoreMessageResult storeMessage(ParsedMessage parsedMessage) throws Exception {
        UUID conversationId = parsedMessage.getConversationId();
        UUID messageId = parsedMessage.getMessageId();

        activityService.captureConversationActivity(conversationId);

        try {
            PresignedUrl presignedUrl = ehrRepoClient.fetchStorageUrl(conversationId, messageId);
            log.info("Retrieved Presigned URL");
            presignedUrl.uploadMessage(parsedMessage);
            log.info("Uploaded message to S3");
            var confirmedMessageStored = ehrRepoClient.confirmMessageStored(parsedMessage);
            log.info("Message stored in EHR Repo");
            return new StoreMessageResult(confirmedMessageStored);
        } catch (Exception exception) {
            sendNegativeAcknowledgement(exception, conversationId);
            throw exception;
        }
    }

    @Deprecated
    public void softDeleteEhrRecord(String nhsNumber) {
        this.ehrRepoClient.softDeleteEhrRecord(nhsNumber);
    }

    private void sendNegativeAcknowledgement(Exception exception, UUID conversationId) {
        switch (exception) {
            case DuplicateMessageException e ->
                    gp2gpMessengerService.sendNegativeAcknowledgement(conversationId, ERROR_CODE_12);
            default -> {} // Do nothing
        }
    }
}
