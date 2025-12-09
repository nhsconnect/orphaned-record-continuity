package uk.nhs.prm.repo.ehrtransferservice.exceptions.database;

import uk.nhs.prm.repo.ehrtransferservice.exceptions.base.DatabaseException;

import java.util.UUID;

public class ConversationNotPresentException extends DatabaseException {
    private static final String EXCEPTION_MESSAGE =
        "No transfer present for Inbound Conversation ID %s";

    public ConversationNotPresentException(UUID inboundConversationId) {
        super(EXCEPTION_MESSAGE.formatted(inboundConversationId.toString().toUpperCase()));
    }
}
