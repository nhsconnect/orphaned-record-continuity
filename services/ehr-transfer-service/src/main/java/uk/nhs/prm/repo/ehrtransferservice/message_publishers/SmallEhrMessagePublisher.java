package uk.nhs.prm.repo.ehrtransferservice.message_publishers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class SmallEhrMessagePublisher {
    private final String smallEhrTopicArn;
    private final MessagePublisher messagePublisher;

    public SmallEhrMessagePublisher(MessagePublisher messagePublisher, @Value("${aws.smallEhrTopicArn}") String smallEhrTopicArn) {
        this.messagePublisher = messagePublisher;
        this.smallEhrTopicArn = smallEhrTopicArn;
    }

    public void sendMessage(String message, UUID conversationId) {
        messagePublisher.sendMessage(this.smallEhrTopicArn, message, Map.of("conversationId", conversationId.toString().toUpperCase()));
    }
}

