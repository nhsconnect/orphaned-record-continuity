package uk.nhs.prm.repo.suspension.service.publishers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.nhs.prm.repo.suspension.service.model.NonSensitiveDataMessage;


@Component
public class NotSuspendedEventPublisher {
    private final String notSuspensionsSnsTopicArn;
    private final MessagePublisher messagePublisher;

    public NotSuspendedEventPublisher(MessagePublisher messagePublisher, @Value("${aws.notSuspendedSnsTopicArn}") String notSuspensionsSnsTopicArn) {
        this.messagePublisher = messagePublisher;

        this.notSuspensionsSnsTopicArn = notSuspensionsSnsTopicArn;
    }

    public void sendMessage(NonSensitiveDataMessage message) {
        messagePublisher.sendMessage(this.notSuspensionsSnsTopicArn, message.toJsonString());
    }
}
