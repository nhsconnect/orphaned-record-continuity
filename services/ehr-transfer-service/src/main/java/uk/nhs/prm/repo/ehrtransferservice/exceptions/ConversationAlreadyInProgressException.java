package uk.nhs.prm.repo.ehrtransferservice.exceptions;

import java.util.UUID;

public class ConversationAlreadyInProgressException extends Exception {
    private static final String EXCEPTION_MESSAGE =
            "Transfer is already in progress for Inbound Conversation ID: %s";

    public ConversationAlreadyInProgressException(UUID inboundConversationId) {
        super(EXCEPTION_MESSAGE.formatted(inboundConversationId.toString().toUpperCase()));
    }
}
