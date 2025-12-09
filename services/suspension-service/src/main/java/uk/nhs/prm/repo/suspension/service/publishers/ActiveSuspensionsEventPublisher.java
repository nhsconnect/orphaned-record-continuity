package uk.nhs.prm.repo.suspension.service.publishers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.nhs.prm.repo.suspension.service.model.ActiveSuspensionsMessage;

@Component
public class ActiveSuspensionsEventPublisher {
    private final String activeSuspensionsSnsTopicArn;
    private final MessagePublisher messagePublisher;

    public ActiveSuspensionsEventPublisher(MessagePublisher messagePublisher, @Value("${aws.activeSuspensionsSnsTopicArn}") String activeSuspensionsSnsTopicArn) {
        this.messagePublisher = messagePublisher;
        this.activeSuspensionsSnsTopicArn = activeSuspensionsSnsTopicArn;
    }

    public void sendMessage(ActiveSuspensionsMessage message, String nemsMessageId) {
        messagePublisher.sendMessage(this.activeSuspensionsSnsTopicArn, message.toJsonString(), "nemsMessageId", nemsMessageId);
    }
}