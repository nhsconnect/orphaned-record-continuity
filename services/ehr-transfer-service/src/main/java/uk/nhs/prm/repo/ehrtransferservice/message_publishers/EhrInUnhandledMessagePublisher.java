package uk.nhs.prm.repo.ehrtransferservice.message_publishers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class EhrInUnhandledMessagePublisher {
    private final String ehrInUnhandledTopicArn;
    private final MessagePublisher messagePublisher;

    public EhrInUnhandledMessagePublisher(MessagePublisher messagePublisher, @Value("${aws.ehrInUnhandledTopicArn}") String ehrInUnhandledTopicArn) {
        this.messagePublisher = messagePublisher;
        this.ehrInUnhandledTopicArn = ehrInUnhandledTopicArn;
    }

    public void sendMessage(String message, UUID conversationId) {
        messagePublisher.sendMessage(this.ehrInUnhandledTopicArn, message, Map.of("conversationId", conversationId.toString().toUpperCase()));
    }
}
