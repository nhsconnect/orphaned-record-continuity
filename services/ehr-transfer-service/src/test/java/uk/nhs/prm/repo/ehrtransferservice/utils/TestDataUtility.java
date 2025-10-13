package uk.nhs.prm.repo.ehrtransferservice.utils;

import uk.nhs.prm.repo.ehrtransferservice.repo_incoming.RepoIncomingEvent;

import java.util.UUID;

public final class TestDataUtility {
    private TestDataUtility() { }

    public static final String NHS_NUMBER = "9798547485";
    public static final String SOURCE_GP = "B45744";
    public static final String NEMS_MESSAGE_ID = "2d74a113-1076-4c63-91bc-e50d232b6a79";
    public static final String REPOSITORY_ODS_CODE = "REPOSITORY_ODS_CODE";
    public static final String REPOSITORY_ASID = "REPOSITORY_ASID";

    public static RepoIncomingEvent createRepoIncomingEvent(UUID inboundConversationId) {
        return RepoIncomingEvent.builder()
            .nhsNumber(NHS_NUMBER)
            .sourceGp(SOURCE_GP)
            .nemsMessageId(NEMS_MESSAGE_ID)
            .conversationId(inboundConversationId.toString().toUpperCase())
            .build();
    }
}