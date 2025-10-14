package uk.nhs.prm.deductions.nemseventprocessor.reregistration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.nhs.prm.deductions.nemseventprocessor.MessagePublisher;

@Component
public class ReRegistrationEventPublisher {
    private final String reRegistrationSnsTopicArn;
    private final MessagePublisher messagePublisher;

    public ReRegistrationEventPublisher(MessagePublisher messagePublisher, @Value("${aws.reRegistrationSnsTopicArn}") String reRegistrationSnsTopicArn) {
        this.messagePublisher = messagePublisher;
        this.reRegistrationSnsTopicArn = reRegistrationSnsTopicArn;
    }

    public void sendMessage(ReRegistrationEvent reRegistrationEvent) {
        messagePublisher.sendMessage(reRegistrationSnsTopicArn, reRegistrationEvent.toJsonString(), "nemsMessageId", reRegistrationEvent.getNemsMessageId());
    }
}
