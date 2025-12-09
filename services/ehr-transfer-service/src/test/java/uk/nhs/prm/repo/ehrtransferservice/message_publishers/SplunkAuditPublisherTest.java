package uk.nhs.prm.repo.ehrtransferservice.message_publishers;

import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.repo.ehrtransferservice.models.SplunkAuditMessage;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static uk.nhs.prm.repo.ehrtransferservice.database.enumeration.ConversationTransferStatus.INBOUND_STARTED;

@ExtendWith(MockitoExtension.class)
class SplunkAuditPublisherTest {
    @Mock
    MessagePublisher messagePublisher;

    String auditTopicArn = "audit-topic-arn";

    SplunkAuditPublisher splunkAuditPublisher;

    @BeforeEach
    void setup() {
        splunkAuditPublisher = new SplunkAuditPublisher(messagePublisher, auditTopicArn);
    }

    @Test
    void shouldInvokeCallToPublishMessageToTheTopicWhenSendMessageIsInvoked() {
        // given
        final UUID inboundConversationId = UUID.randomUUID();
        final Optional<UUID> nemsMessageId = Optional.of(UUID.randomUUID());
        final SplunkAuditMessage splunkAuditMessage = new SplunkAuditMessage(
            inboundConversationId,
            INBOUND_STARTED,
            nemsMessageId
        );

        // when
        splunkAuditPublisher.sendMessage(splunkAuditMessage);

        // then
        verify(messagePublisher).sendMessage(auditTopicArn, new Gson().toJson(splunkAuditMessage));
    }
}