package uk.nhs.prm.repo.ehrtransferservice.message_publishers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class LargeEhrMessagePublisher {
    private final String largeEhrTopicArn;
    private final MessagePublisher messagePublisher;

    public LargeEhrMessagePublisher(MessagePublisher messagePublisher, @Value("${aws.largeEhrTopicArn}") String largeEhrTopicArn) {
        this.messagePublisher = messagePublisher;
        this.largeEhrTopicArn = largeEhrTopicArn;
    }

    public void sendMessage(String message, UUID conversationId) {
        messagePublisher.sendMessage(this.largeEhrTopicArn, message, Map.of("conversationId", conversationId.toString().toUpperCase()));
    }
}

