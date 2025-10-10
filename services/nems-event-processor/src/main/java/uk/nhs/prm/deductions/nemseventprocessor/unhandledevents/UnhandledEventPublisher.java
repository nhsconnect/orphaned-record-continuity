package uk.nhs.prm.deductions.nemseventprocessor.unhandledevents;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.nhs.prm.deductions.nemseventprocessor.MessagePublisher;
import uk.nhs.prm.deductions.nemseventprocessor.nemsevents.NonSuspendedMessage;

@Component
@Slf4j
public class UnhandledEventPublisher {
    private final String unhandledEventsSnsTopicArn;
    private final MessagePublisher messagePublisher;

    public UnhandledEventPublisher(MessagePublisher messagePublisher, @Value("${aws.unhandledEventsSnsTopicArn}") String unhandledEventsSnsTopicArn) {
        this.messagePublisher = messagePublisher;
        this.unhandledEventsSnsTopicArn = unhandledEventsSnsTopicArn;
    }

    public void sendMessage(NonSuspendedMessage message, String reasonUnhandled) {
        messagePublisher.sendMessage(this.unhandledEventsSnsTopicArn, message.toJsonString(), "reasonUnhandled", reasonUnhandled);
    }
}
