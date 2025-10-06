package uk.nhs.prm.repo.ehrtransferservice.message_publishers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class NegativeAcknowledgementMessagePublisher {
    private final String negativeAcksTopicArn;
    private final MessagePublisher messagePublisher;

    public NegativeAcknowledgementMessagePublisher(MessagePublisher messagePublisher, @Value("${aws.negativeAcksTopicArn}") String negativeAcksTopicArn) {
        this.messagePublisher = messagePublisher;
        this.negativeAcksTopicArn = negativeAcksTopicArn;
    }

    public void sendMessage(String message, UUID conversationId) {
        messagePublisher.sendMessage(this.negativeAcksTopicArn, message, Map.of("conversationId", conversationId.toString().toUpperCase()));
    }
}

