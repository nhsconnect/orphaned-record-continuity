package uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Gp2gpMessengerEhrRequestBody {
    private final String repositoryOdsCode;
    private final String repositoryAsid;
    private final String practiceOdsCode;
    private final String conversationId;

    public Gp2gpMessengerEhrRequestBody(@JsonProperty("repositoryOdsCode") String repositoryOdsCode,
                                        @JsonProperty("repositoryAsid") String repositoryAsid,
                                        @JsonProperty("practiceOdsCode") String practiceOdsCode,
                                        @JsonProperty("conversationId") String conversationId
    ) {
        this.repositoryOdsCode = repositoryOdsCode;
        this.repositoryAsid = repositoryAsid;
        this.practiceOdsCode = practiceOdsCode;
        this.conversationId = conversationId;
    }

}
