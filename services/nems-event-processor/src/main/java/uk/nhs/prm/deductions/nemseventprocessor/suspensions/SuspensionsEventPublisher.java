package uk.nhs.prm.deductions.nemseventprocessor.suspensions;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.nhs.prm.deductions.nemseventprocessor.MessagePublisher;
import uk.nhs.prm.deductions.nemseventprocessor.nemsevents.SuspendedMessage;

@Component
public class SuspensionsEventPublisher {
    private final String suspensionsSnsTopicArn;
    private final MessagePublisher messagePublisher;

    public SuspensionsEventPublisher(MessagePublisher messagePublisher, @Value("${aws.suspensionsSnsTopicArn}") String suspensionsSnsTopicArn) {
        this.messagePublisher = messagePublisher;
        this.suspensionsSnsTopicArn = suspensionsSnsTopicArn;
    }

    public void sendMessage(SuspendedMessage suspendedMessage) {
        messagePublisher.sendMessage(this.suspensionsSnsTopicArn, suspendedMessage.toJsonString(), "nemsMessageId", suspendedMessage.getNemsMessageId());
    }
}
