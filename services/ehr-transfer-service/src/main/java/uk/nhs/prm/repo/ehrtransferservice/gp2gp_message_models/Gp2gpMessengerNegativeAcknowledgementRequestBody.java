package uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models;

import lombok.Getter;
import uk.nhs.prm.repo.ehrtransferservice.models.enums.AcknowledgementErrorCode;

@Getter
public class Gp2gpMessengerNegativeAcknowledgementRequestBody extends Gp2gpMessengerAcknowledgementRequestBody {
    private final String errorCode;
    private final String errorDisplayName;

    public Gp2gpMessengerNegativeAcknowledgementRequestBody(
            String repositoryAsid,
            String odsCode,
            String conversationId,
            String messageId,
            AcknowledgementErrorCode acknowledgementErrorCode
    ) {
        super(repositoryAsid, odsCode, conversationId, messageId);
        this.errorCode = acknowledgementErrorCode.errorCode;
        this.errorDisplayName = acknowledgementErrorCode.displayName;
    }
}
