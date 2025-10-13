package uk.nhs.prm.repo.ehrtransferservice.models;

import lombok.Data;
import uk.nhs.prm.repo.ehrtransferservice.database.enumeration.ConversationTransferStatus;

import java.util.Optional;
import java.util.UUID;

@Data
public class SplunkAuditMessage {
    private String conversationId;
    private String status;
    private String nemsMessageId;

    public SplunkAuditMessage(UUID conversationId, ConversationTransferStatus transferStatus, Optional<UUID> nemsMessageId) {
        this.conversationId = conversationId.toString().toUpperCase();
        this.status = transferStatus.name();
        this.nemsMessageId = nemsMessageId.isPresent()
            ? nemsMessageId.toString()
            : "NO NEMS MESSAGE ID";
    }
}