package uk.nhs.prm.repo.suspension.service.publishers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.repo.suspension.service.model.RepoIncomingEvent;

import static org.mockito.Mockito.verify;
@ExtendWith(MockitoExtension.class)
class RepoIncomingEventPublisherTest {
    @Mock
    private MessagePublisher messagePublisher;

    private final static String topicArn = "topicArn";
    private RepoIncomingEventPublisher repoIncomingEventPublisher;

    @BeforeEach
    void setUp() {
        repoIncomingEventPublisher = new RepoIncomingEventPublisher(messagePublisher, topicArn);
    }

    @Test
    void shouldPublishStringMessageToTopic() {
        var repoIncomingEvent = new RepoIncomingEvent("nhsNumber", "DGP123", "SRC123", "NEMS123", "C123", "lastUpdated");
        String messageBody = "{\"nhsNumber\":\"nhsNumber\",\"destinationGp\":\"DGP123\",\"sourceGp\":\"SRC123\",\"nemsMessageId\":\"NEMS123\",\"conversationId\":\"C123\",\"nemsEventLastUpdated\":\"lastUpdated\"}";
        repoIncomingEventPublisher.sendMessage(repoIncomingEvent);
        verify(messagePublisher).sendMessage(topicArn, messageBody, "conversationId", "C123");
    }

}