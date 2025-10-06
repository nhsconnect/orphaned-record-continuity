package uk.nhs.prm.repo.ehrtransferservice.exceptions.acknowledgement;

import uk.nhs.prm.repo.ehrtransferservice.exceptions.base.AcknowledgementException;

import java.util.UUID;

public class NegativeAcknowledgementFailedException extends AcknowledgementException {
    private static final String EXCEPTION_MESSAGE =
        "Failed to send a Negative Acknowledgement with error code %s for Inbound Conversation ID %s";

    public NegativeAcknowledgementFailedException(String errorCode, UUID inboundConversationId, Throwable cause) {
        super(EXCEPTION_MESSAGE.formatted(errorCode, inboundConversationId.toString().toUpperCase()), cause);
    }
}
