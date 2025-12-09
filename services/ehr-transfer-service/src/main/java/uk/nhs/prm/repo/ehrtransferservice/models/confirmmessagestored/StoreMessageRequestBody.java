package uk.nhs.prm.repo.ehrtransferservice.models.confirmmessagestored;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.List;
import java.util.UUID;

public class StoreMessageRequestBody {

    @SuppressFBWarnings(value = "URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD")
    public StoreMessageData data;

    public StoreMessageRequestBody(UUID messageId, UUID conversationId, String nhsNumber, String messageType, List<UUID> fragmentMessageIds) {
        this.data = new StoreMessageData(messageId, conversationId, nhsNumber, messageType, fragmentMessageIds);
    }
}
