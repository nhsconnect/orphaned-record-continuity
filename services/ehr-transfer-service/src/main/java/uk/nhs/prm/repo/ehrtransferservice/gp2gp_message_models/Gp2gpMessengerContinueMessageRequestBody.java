package uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.UUID;

@Data
public class Gp2gpMessengerContinueMessageRequestBody {
    private final UUID conversationId;
    private final String gpOdsCode;
    private final UUID ehrExtractMessageId;


    public Gp2gpMessengerContinueMessageRequestBody(@JsonProperty("conversationId") UUID conversationId,
                                                    @JsonProperty("gpOdsCode") String gpOdsCode,
                                                    @JsonProperty("ehrExtractMessageId") UUID ehrExtractMessageId
    ) {
        this.conversationId = conversationId;
        this.gpOdsCode = gpOdsCode;
        this.ehrExtractMessageId = ehrExtractMessageId;
    }
}
