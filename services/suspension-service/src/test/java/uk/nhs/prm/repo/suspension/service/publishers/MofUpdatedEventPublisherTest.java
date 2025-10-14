package uk.nhs.prm.repo.suspension.service.publishers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.repo.suspension.service.model.ManagingOrganisationUpdatedMessage;
import uk.nhs.prm.repo.suspension.service.publishers.MessagePublisher;
import uk.nhs.prm.repo.suspension.service.publishers.MofUpdatedEventPublisher;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MofUpdatedEventPublisherTest {

    @Mock
    private MessagePublisher messagePublisher;

    private final static String suspensionsTopicArn = "suspensionsTopicArn";

    private MofUpdatedEventPublisher mofUpdatedEventPublisher;

    @BeforeEach
    void setUp() {
        mofUpdatedEventPublisher = new MofUpdatedEventPublisher(messagePublisher, suspensionsTopicArn);
    }

    @Test
    void shouldPublishMessageToTheUnhandledTopic() {
        var mofUpdatedMessage = new ManagingOrganisationUpdatedMessage("nemsMessageId","anything", "status");
        String NonSensitiveMessageBody = "{\"nemsMessageId\":\"nemsMessageId\",\"managingOrganisationOdsCode\":\"anything\",\"status\":\"status\"}";
        mofUpdatedEventPublisher.sendMessage(mofUpdatedMessage);
        verify(messagePublisher).sendMessage(suspensionsTopicArn, NonSensitiveMessageBody);
    }
}