package uk.nhs.prm.repo.ehrtransferservice.services.gp2gp_messenger;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.nhs.prm.repo.ehrtransferservice.database.TransferService;
import uk.nhs.prm.repo.ehrtransferservice.database.model.ConversationRecord;
import uk.nhs.prm.repo.ehrtransferservice.exceptions.HttpException;
import uk.nhs.prm.repo.ehrtransferservice.exceptions.acknowledgement.EhrCompleteAcknowledgementFailedException;
import uk.nhs.prm.repo.ehrtransferservice.exceptions.acknowledgement.NegativeAcknowledgementFailedException;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.*;
import uk.nhs.prm.repo.ehrtransferservice.models.enums.AcknowledgementErrorCode;
import uk.nhs.prm.repo.ehrtransferservice.repo_incoming.RepoIncomingEvent;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class Gp2gpMessengerService {
    private final Gp2gpMessengerClient gp2gpMessengerClient;
    private final TransferService transferService;

    @Value("${repositoryOdsCode}")
    private String repositoryOdsCode;

    @Value("${repositoryAsid}")
    private String repositoryAsid;

    public void sendEhrRequest(RepoIncomingEvent repoIncomingEvent) throws Exception {
        final UUID inboundConversationId = UUID.fromString(repoIncomingEvent.getConversationId());
        final Gp2gpMessengerEhrRequestBody requestBody = new Gp2gpMessengerEhrRequestBody(
            repositoryOdsCode,
            repositoryAsid,
            repoIncomingEvent.getSourceGp(),
            repoIncomingEvent.getConversationId().toUpperCase()
        );

        try {
            gp2gpMessengerClient.sendGp2gpMessengerEhrRequest(repoIncomingEvent.getNhsNumber(), requestBody);
            log.info("EHR Request sent for Inbound Conversation ID {}", inboundConversationId.toString().toUpperCase());
        } catch (Exception exception) {
            throw new Exception(
                "Error while sending EHR Request for Inbound Conversation ID %s".formatted(inboundConversationId.toString().toUpperCase()),
                exception
            );
        }
    }

    public void sendContinueMessage(ParsedMessage parsedMessage, String sourceGp)
        throws HttpException, IOException, URISyntaxException, InterruptedException {
        Gp2gpMessengerContinueMessageRequestBody continueMessageRequestBody = new Gp2gpMessengerContinueMessageRequestBody(
            parsedMessage.getConversationId(),
            sourceGp,
            parsedMessage.getMessageId()
        );

        gp2gpMessengerClient.sendContinueMessage(continueMessageRequestBody);
        log.info("Continue Request sent for Inbound Conversation ID {}", parsedMessage.getConversationId());
    }

    public void sendEhrCompletePositiveAcknowledgement(UUID inboundConversationId) {
        final ConversationRecord record =
            transferService.getConversationByInboundConversationId(inboundConversationId);

        final UUID ehrCoreMessageId =
            transferService.getEhrCoreInboundMessageIdForInboundConversationId(inboundConversationId);

        final var requestBody = new Gp2gpMessengerPositiveAcknowledgementRequestBody(
            repositoryAsid,
            record.sourceGp(),
            inboundConversationId.toString().toUpperCase(),
            ehrCoreMessageId.toString().toUpperCase()
        );

        try {
            gp2gpMessengerClient.sendGp2gpMessengerAcknowledgement(record.nhsNumber(), requestBody);
            log.info("EHR complete positive acknowledgement sent for Inbound Conversation ID {}", inboundConversationId.toString().toUpperCase());
        } catch (IOException | URISyntaxException | InterruptedException | HttpException exception) {
            log.error("An exception occurred while sending an EHR complete positive acknowledgement {}", exception.getMessage());
            throw new EhrCompleteAcknowledgementFailedException(inboundConversationId, exception);
        }
    }

    public void sendNegativeAcknowledgement(
            UUID inboundConversationId,
            AcknowledgementErrorCode acknowledgementErrorCode
    ) {
        final ConversationRecord record =
                transferService.getConversationByInboundConversationId(inboundConversationId);

        final UUID ehrCoreMessageId =
                transferService.getEhrCoreInboundMessageIdForInboundConversationId(inboundConversationId);

        final var requestBody = new Gp2gpMessengerNegativeAcknowledgementRequestBody(
                repositoryAsid,
                record.sourceGp(),
                inboundConversationId.toString().toUpperCase(),
                ehrCoreMessageId.toString().toUpperCase(),
                acknowledgementErrorCode
        );

        try {
            gp2gpMessengerClient.sendGp2gpMessengerAcknowledgement(record.nhsNumber(), requestBody);
            log.info("Negative acknowledgement with code {} sent for Inbound Conversation ID {}",
                    requestBody.getErrorCode(), inboundConversationId.toString().toUpperCase());
        } catch (IOException | URISyntaxException | InterruptedException | HttpException exception) {
            log.error("An exception occurred while sending a negative acknowledgement with code {} " +
                            "sent for Inbound Conversation ID {}. Exception message is: {}",
                    requestBody.getErrorCode(), inboundConversationId.toString().toUpperCase(), exception.getMessage());
            throw new NegativeAcknowledgementFailedException(acknowledgementErrorCode.errorCode, inboundConversationId, exception);
        }
    }
}