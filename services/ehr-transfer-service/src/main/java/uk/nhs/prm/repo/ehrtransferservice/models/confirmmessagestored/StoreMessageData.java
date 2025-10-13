package uk.nhs.prm.repo.ehrtransferservice.models.confirmmessagestored;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.List;
import java.util.UUID;

public class StoreMessageData {

    @SuppressFBWarnings(value = "URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD")
    public String type;
    @SuppressFBWarnings(value = "URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD")
    public UUID id;
    @SuppressFBWarnings(value = "URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD")
    public StoreMessageAttributes attributes;

    public StoreMessageData(UUID messageId, UUID conversationId, String nhsNumber, String messageType, List<UUID> fragmentMessageIds) {
        this.type = "messages";
        this.id = messageId;
        this.attributes = new StoreMessageAttributes(conversationId, nhsNumber, messageType, fragmentMessageIds);
    }
}
