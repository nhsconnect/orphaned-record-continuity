package uk.nhs.prm.repo.suspension.service.publishers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.repo.suspension.service.model.NonSensitiveDataMessage;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DeceasedPatientEventPublisherTest {

    @Mock
    private MessagePublisher messagePublisher;

    private final static String deceasedPatientTopicArn = "suspensionsTopicArn";

    private DeceasedPatientEventPublisher deceasedPatientEventPublisher;

    @BeforeEach
    void setUp() {
        deceasedPatientEventPublisher = new DeceasedPatientEventPublisher(messagePublisher, deceasedPatientTopicArn);
    }

    @Test
    void shouldPublishMessageToTheUnhandledTopic() {
        var nonSensitiveDataMessage = new NonSensitiveDataMessage("nemsMessageId","deceased");
        String NonSensitiveMessageBody = "{\"nemsMessageId\":\"nemsMessageId\",\"status\":\"deceased\"}";
        deceasedPatientEventPublisher.sendMessage(nonSensitiveDataMessage);
        verify(messagePublisher).sendMessage(deceasedPatientTopicArn, NonSensitiveMessageBody);
    }
}