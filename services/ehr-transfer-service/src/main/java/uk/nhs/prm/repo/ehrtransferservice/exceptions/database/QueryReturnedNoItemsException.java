package uk.nhs.prm.repo.ehrtransferservice.exceptions.database;

import uk.nhs.prm.repo.ehrtransferservice.exceptions.base.DatabaseException;

import java.util.UUID;

public class QueryReturnedNoItemsException extends DatabaseException {
    private static final String EXCEPTION_MESSAGE = "The query returned no items for Inbound Conversation ID %s";

    public QueryReturnedNoItemsException(UUID inboundConversationId) {
        super(EXCEPTION_MESSAGE.formatted(inboundConversationId.toString().toUpperCase()));
    }
}