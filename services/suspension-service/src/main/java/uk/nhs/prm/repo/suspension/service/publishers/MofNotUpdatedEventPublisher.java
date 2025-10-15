package uk.nhs.prm.repo.suspension.service.publishers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.nhs.prm.repo.suspension.service.model.NonSensitiveDataMessage;

@Component
public class MofNotUpdatedEventPublisher {
    private final String mofNotUpdatedSnsTopicArn;
    private final MessagePublisher messagePublisher;

    public MofNotUpdatedEventPublisher(MessagePublisher messagePublisher, @Value("${aws.mofNotUpdatedSnsTopicArn}") String mofNotUpdatedSnsTopicArn) {
        this.messagePublisher = messagePublisher;
        this.mofNotUpdatedSnsTopicArn = mofNotUpdatedSnsTopicArn;
    }

    public void sendMessage(NonSensitiveDataMessage message) {
        messagePublisher.sendMessage(this.mofNotUpdatedSnsTopicArn, message.toJsonString());
    }
}

