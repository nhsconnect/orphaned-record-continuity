package uk.nhs.prm.repo.ehrtransferservice.message_publishers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EhrInUnhandledMessagePublisherTest {
    @Mock
    private MessagePublisher messagePublisher;

    private final static String topicArn = "topicArn";

    private EhrInUnhandledMessagePublisher ehrInUnhandledMessagePublisher;

    @BeforeEach
    void setUp() {
        ehrInUnhandledMessagePublisher = new EhrInUnhandledMessagePublisher(messagePublisher, topicArn);
    }

    @Test
    void shouldPublishMessageToThePositiveAcknowledgmentTopic() {
        var conversationId = UUID.randomUUID();
        ehrInUnhandledMessagePublisher.sendMessage("message", conversationId);
        verify(messagePublisher).sendMessage(topicArn, "message", Map.of("conversationId", conversationId.toString().toUpperCase()));
    }
}