package uk.nhs.prm.repo.ehrtransferservice.exceptions.timeout;

import uk.nhs.prm.repo.ehrtransferservice.exceptions.base.TimeoutException;

import java.util.UUID;

public class TimeoutExceededException extends TimeoutException {
    private static final String EXCEPTION_MESSAGE = "The request for Inbound Conversation ID %s has timed out";

    public TimeoutExceededException(UUID inboundConversationId) {
        super(EXCEPTION_MESSAGE.formatted(inboundConversationId.toString().toUpperCase()));
    }
}