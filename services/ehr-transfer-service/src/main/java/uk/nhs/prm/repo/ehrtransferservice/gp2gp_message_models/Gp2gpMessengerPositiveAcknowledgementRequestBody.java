package uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Gp2gpMessengerPositiveAcknowledgementRequestBody extends Gp2gpMessengerAcknowledgementRequestBody{
    public Gp2gpMessengerPositiveAcknowledgementRequestBody(
            String repositoryAsid,
            String odsCode,
            String conversationId,
            String messageId
    ) {
        super(repositoryAsid, odsCode, conversationId, messageId);
    }
}
