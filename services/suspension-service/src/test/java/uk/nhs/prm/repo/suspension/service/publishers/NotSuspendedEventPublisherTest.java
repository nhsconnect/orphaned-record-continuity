package uk.nhs.prm.repo.suspension.service.publishers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.repo.suspension.service.model.NonSensitiveDataMessage;
import uk.nhs.prm.repo.suspension.service.publishers.MessagePublisher;
import uk.nhs.prm.repo.suspension.service.publishers.NotSuspendedEventPublisher;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotSuspendedEventPublisherTest {

    @Mock
    private MessagePublisher messagePublisher;

    private final static String suspensionsTopicArn = "suspensionsTopicArn";

    private NotSuspendedEventPublisher notSuspendedEventPublisher;

    @BeforeEach
    void setUp() {
        notSuspendedEventPublisher = new NotSuspendedEventPublisher(messagePublisher, suspensionsTopicArn);
    }

    @Test
    void shouldPublishMessageToTheUnhandledTopic() {
        var nonSensitiveDataMessage = new NonSensitiveDataMessage("nemsMessageId","status");
        String NonSensitiveMessageBody = "{\"nemsMessageId\":\"nemsMessageId\",\"status\":\"status\"}";
        notSuspendedEventPublisher.sendMessage(nonSensitiveDataMessage);
        verify(messagePublisher).sendMessage(suspensionsTopicArn, NonSensitiveMessageBody);
    }
}