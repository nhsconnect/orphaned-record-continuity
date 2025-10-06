package uk.nhs.prm.repo.ehrtransferservice.message_publishers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class FragmentMessagePublisher {
    private final String fragmentsTopicArn;
    private final MessagePublisher messagePublisher;

    public FragmentMessagePublisher(MessagePublisher messagePublisher, @Value("${aws.largeMessageFragmentsTopicArn}") String fragmentsTopicArn) {
        this.messagePublisher = messagePublisher;
        this.fragmentsTopicArn = fragmentsTopicArn;
    }

    public void sendMessage(String message, UUID conversationId) {
        messagePublisher.sendMessage(this.fragmentsTopicArn, message, Map.of("conversationId", conversationId.toString().toUpperCase()));
    }
}

