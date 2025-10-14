package uk.nhs.prm.deductions.nemseventprocessor.dlq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.nhs.prm.deductions.nemseventprocessor.MessagePublisher;

@Component
@Slf4j
public class DeadLetterQueuePublisher {
    private final String deadLetterQueueSnsTopicArn;
    private final MessagePublisher messagePublisher;

    public DeadLetterQueuePublisher(MessagePublisher messagePublisher, @Value("${aws.deadLetterQueueSnsTopicArn}") String deadLetterQueueSnsTopicArn) {
        this.messagePublisher = messagePublisher;
        this.deadLetterQueueSnsTopicArn = deadLetterQueueSnsTopicArn;
    }

    public void sendMessage(String message, String reasonCannotProcess) {
        messagePublisher.sendMessage(this.deadLetterQueueSnsTopicArn, message, "reasonCannotProcess", reasonCannotProcess);
    }
}
