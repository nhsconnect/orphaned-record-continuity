package uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;

@Getter
public abstract class Gp2gpMessengerAcknowledgementRequestBody {
    protected final String repositoryAsid;
    protected final String odsCode;
    protected final String conversationId;
    protected final String messageId;

    protected Gp2gpMessengerAcknowledgementRequestBody(
            @JsonProperty("repositoryAsid") String repositoryAsid,
            @JsonProperty("odsCode") String odsCode,
            @JsonProperty("conversationId") String conversationId,
            @JsonProperty("messageId") String messageId
    ) {
        this.repositoryAsid = repositoryAsid;
        this.odsCode = odsCode;
        this.conversationId = conversationId;
        this.messageId = messageId;
    }
}
