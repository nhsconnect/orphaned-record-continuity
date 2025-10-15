package uk.nhs.prm.repo.suspension.service.publishers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.nhs.prm.repo.suspension.service.model.ManagingOrganisationUpdatedMessage;

@Component
public class MofUpdatedEventPublisher {
    private final String mofUpdatedSnsTopicArn;
    private final MessagePublisher messagePublisher;

    public MofUpdatedEventPublisher(MessagePublisher messagePublisher, @Value("${aws.mofUpdatedSnsTopicArn}") String mofUpdatedSnsTopicArn) {
        this.messagePublisher = messagePublisher;
        this.mofUpdatedSnsTopicArn = mofUpdatedSnsTopicArn;
    }

    public void sendMessage(ManagingOrganisationUpdatedMessage message) {
        messagePublisher.sendMessage(this.mofUpdatedSnsTopicArn, message.toJsonString());
    }
}

