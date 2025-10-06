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
class SmallEhrMessagePublisherTest {
    @Mock
    private MessagePublisher messagePublisher;

    private final static String smallEhrTopicArn = "smallEhrTopicArn";

    private SmallEhrMessagePublisher smallEhrMessagePublisher;
    private UUID conversationId;

    SmallEhrMessagePublisherTest() {
        conversationId = UUID.randomUUID();
    }

    @BeforeEach
    void setUp() {
        smallEhrMessagePublisher = new SmallEhrMessagePublisher(messagePublisher, smallEhrTopicArn);
    }


    @Test
    void shouldPublishMessageToTheSmallEhrTopic() {
        smallEhrMessagePublisher.sendMessage("message", conversationId);
        verify(messagePublisher).sendMessage(smallEhrTopicArn, "message", Map.of("conversationId", conversationId.toString().toUpperCase()));
    }
}