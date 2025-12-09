package uk.nhs.prm.repo.ehrtransferservice.exceptions.database;

import uk.nhs.prm.repo.ehrtransferservice.exceptions.base.DatabaseException;

import java.util.UUID;

public class ConversationAlreadyPresentException extends DatabaseException {
    private static final String EXCEPTION_MESSAGE =
        "The conversation already exists for Inbound Conversation ID %s";

    public ConversationAlreadyPresentException(UUID inboundConversationId) {
        super(EXCEPTION_MESSAGE.formatted(inboundConversationId.toString().toUpperCase()));
    }
}