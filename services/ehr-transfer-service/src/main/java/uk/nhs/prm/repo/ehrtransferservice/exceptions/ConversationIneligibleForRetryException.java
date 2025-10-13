package uk.nhs.prm.repo.ehrtransferservice.exceptions;

import java.util.UUID;

public class ConversationIneligibleForRetryException extends Exception {
    private static final String EXCEPTION_MESSAGE =
            "RepoIncomingEvent is not eligible for retry for Inbound Conversation ID: %s";

    public ConversationIneligibleForRetryException(UUID inboundConversationId) {
        super(EXCEPTION_MESSAGE.formatted(inboundConversationId.toString().toUpperCase()));
    }
}