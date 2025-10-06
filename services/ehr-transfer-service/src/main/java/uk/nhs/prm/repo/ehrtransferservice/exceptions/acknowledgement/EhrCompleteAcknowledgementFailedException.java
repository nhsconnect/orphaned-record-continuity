package uk.nhs.prm.repo.ehrtransferservice.exceptions.acknowledgement;

import uk.nhs.prm.repo.ehrtransferservice.exceptions.base.AcknowledgementException;

import java.util.UUID;

public class EhrCompleteAcknowledgementFailedException extends AcknowledgementException {
    private static final String EXCEPTION_MESSAGE =
        "Failed to send an EHR Complete Acknowledgement for Inbound Conversation ID %s";

    public EhrCompleteAcknowledgementFailedException(UUID inboundConversationId, Throwable cause) {
        super(EXCEPTION_MESSAGE.formatted(inboundConversationId.toString().toUpperCase()), cause);
    }
}
