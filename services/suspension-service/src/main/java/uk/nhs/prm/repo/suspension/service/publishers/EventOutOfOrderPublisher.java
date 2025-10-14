package uk.nhs.prm.repo.suspension.service.publishers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.nhs.prm.repo.suspension.service.model.NonSensitiveDataMessage;

@Component
public class EventOutOfOrderPublisher {
    private final String eventOutOfOrderSnsTopicArn;
    private final MessagePublisher messagePublisher;

    public EventOutOfOrderPublisher(MessagePublisher messagePublisher, @Value("${aws.eventOutOrderSnsTopicArn}") String eventOutOfOrderSnsTopicArn) {
        this.messagePublisher = messagePublisher;
        this.eventOutOfOrderSnsTopicArn = eventOutOfOrderSnsTopicArn;
    }

    public void sendMessage(NonSensitiveDataMessage message) {
        messagePublisher.sendMessage(this.eventOutOfOrderSnsTopicArn, message.toJsonString());
    }
}

