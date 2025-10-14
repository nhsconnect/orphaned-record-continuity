package uk.nhs.prm.repo.suspension.service.publishers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.nhs.prm.repo.suspension.service.model.RepoIncomingEvent;

@Component
public class RepoIncomingEventPublisher {
    private final MessagePublisher messagePublisher;
    private final String repoIncomingSnsTopicArn;


    public RepoIncomingEventPublisher(MessagePublisher messagePublisher,
                                      @Value("${aws.repoIncomingSnsTopicArn}") String repoIncomingSnsTopicArn) {
        this.messagePublisher = messagePublisher;
        this.repoIncomingSnsTopicArn = repoIncomingSnsTopicArn;
    }

    public void sendMessage(RepoIncomingEvent repoIncomingEvent) {
        messagePublisher.sendMessage(this.repoIncomingSnsTopicArn, repoIncomingEvent.toJsonString(), "conversationId", repoIncomingEvent.getConversationId());

    }
}
