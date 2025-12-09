package uk.nhs.prm.repo.ehrtransferservice.message_publishers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class PositiveAcknowledgementMessagePublisher {
    private final String positiveAcksTopicArn;
    private final MessagePublisher messagePublisher;

    public PositiveAcknowledgementMessagePublisher(MessagePublisher messagePublisher, @Value("${aws.positiveAcksTopicArn}") String positiveAcksTopicArn) {
        this.messagePublisher = messagePublisher;
        this.positiveAcksTopicArn = positiveAcksTopicArn;
    }

    public void sendMessage(String message, UUID conversationId) {
        messagePublisher.sendMessage(this.positiveAcksTopicArn, message, Map.of("conversationId", conversationId.toString().toUpperCase()));
    }
}

