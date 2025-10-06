package uk.nhs.prm.repo.ehrtransferservice.message_publishers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ParsingDlqPublisher {
    private final String parsingDlqTopicArn;
    private final MessagePublisher messagePublisher;

    public ParsingDlqPublisher(MessagePublisher messagePublisher, @Value("${aws.parsingDlqTopicArn}") String parsingDlqTopicArn) {
        this.messagePublisher = messagePublisher;
        this.parsingDlqTopicArn = parsingDlqTopicArn;
    }

    public void sendMessage(String message) {
        messagePublisher.sendMessage(this.parsingDlqTopicArn, message);
    }
}

