package uk.nhs.prm.repo.re_registration.model;

import org.junit.jupiter.api.Test;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

class DeleteAuditMessageTest {

    @Test
    void shouldHaveCorrectStatus() {
        var conversationIds = asList("a", "b");
        var deleteAuditMessage = new DeleteAuditMessage("someId", conversationIds);
        assertThat(deleteAuditMessage.getNemsMessageId()).isEqualTo("someId");
        assertThat(deleteAuditMessage.getStatus()).isEqualTo("ACTION:RE_REGISTRATION_EHR_DELETED");
        assertThat(deleteAuditMessage.getConversationIds()).isEqualTo(conversationIds);
    }
}