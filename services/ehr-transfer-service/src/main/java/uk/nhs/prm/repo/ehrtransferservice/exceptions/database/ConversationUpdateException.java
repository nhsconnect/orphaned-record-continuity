package uk.nhs.prm.repo.ehrtransferservice.exceptions.database;

import uk.nhs.prm.repo.ehrtransferservice.exceptions.base.DatabaseException;

import java.util.UUID;

public class ConversationUpdateException extends DatabaseException {
    private static final String EXCEPTION_MESSAGE =
        "The conversation could not be updated with Inbound Conversation ID %s";

    public ConversationUpdateException(UUID inboundConversationId, Throwable throwable) {
        super(EXCEPTION_MESSAGE.formatted(inboundConversationId.toString().toUpperCase()), throwable);
    }
}
