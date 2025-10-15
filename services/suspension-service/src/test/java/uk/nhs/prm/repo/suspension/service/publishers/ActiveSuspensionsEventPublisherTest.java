package uk.nhs.prm.repo.suspension.service.publishers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.repo.suspension.service.model.ActiveSuspensionsMessage;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ActiveSuspensionsEventPublisherTest {

    @Mock
    MessagePublisher messagePublisher;

    private final static String activeSuspensionsTopicArn = "activeSuspensionsTopicArn";

    private ActiveSuspensionsEventPublisher activeSuspensionsEventPublisher;

    @BeforeEach
    void setUp() {
        activeSuspensionsEventPublisher = new ActiveSuspensionsEventPublisher(messagePublisher, activeSuspensionsTopicArn);
    }

    @Test
    void shouldPublishMessageToTheUnhandledTopic() {
        var nemsId = "nemsId";
        var activeSuspensionsMessage = new ActiveSuspensionsMessage("1234567890","anything", "timestamp");
        String activeSuspensionMessage = "{\"nhsNumber\":\"1234567890\",\"previousOdsCode\":\"anything\",\"nemsLastUpdatedDate\":\"timestamp\"}";
        activeSuspensionsEventPublisher.sendMessage(activeSuspensionsMessage, "nemsId");
        verify(messagePublisher).sendMessage(activeSuspensionsTopicArn, activeSuspensionMessage, "nemsMessageId", nemsId);
    }

}