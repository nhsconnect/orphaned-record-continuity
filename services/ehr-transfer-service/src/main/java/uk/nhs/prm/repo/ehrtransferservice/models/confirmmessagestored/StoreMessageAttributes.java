package uk.nhs.prm.repo.ehrtransferservice.models.confirmmessagestored;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.List;
import java.util.UUID;

public class StoreMessageAttributes {
    @SuppressFBWarnings(value = "URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD")
    public UUID conversationId;
    @SuppressFBWarnings(value = "URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD")
    public String messageType;
    @SuppressFBWarnings(value = "URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD")
    public String nhsNumber;
    @SuppressFBWarnings(value = "URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD")
    public List<UUID> fragmentMessageIds;

    public StoreMessageAttributes(UUID conversationId, String nhsNumber, String messageType, List<UUID> fragmentMessageIds) {
        this.conversationId = conversationId;
        this.messageType = messageType;
        this.nhsNumber = nhsNumber;
        this.fragmentMessageIds = fragmentMessageIds;
    }
}
