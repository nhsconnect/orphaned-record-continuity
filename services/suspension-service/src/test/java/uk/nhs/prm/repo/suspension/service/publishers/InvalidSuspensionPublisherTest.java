package uk.nhs.prm.repo.suspension.service.publishers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.repo.suspension.service.config.Tracer;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvalidSuspensionPublisherTest {

    @Mock
    private MessagePublisher messagePublisher;

    @Mock
    private Tracer tracer;

    private final static String invalidSuspensionSensitiveTopic = "invalidTopicArn";
    private final static String invalidSuspensionAuditTopic = "invalidTopicAuditArn";

    private InvalidSuspensionPublisher invalidSuspensionPublisher;

    @BeforeEach
    void setUp() {
        invalidSuspensionPublisher = new InvalidSuspensionPublisher(messagePublisher, invalidSuspensionSensitiveTopic, invalidSuspensionAuditTopic, tracer);
    }

    @Test
    void shouldPublishMessageToBothSensitiveAndAuditTopic() {
        var nemsMessageId = "someId";
        var suspensionMessage = "someString";
        var stringAuditMessage = "{\"nemsMessageId\":\"someId\",\"status\":\"NO_ACTION:INVALID_SUSPENSION\"}";

        invalidSuspensionPublisher.sendInvalidMessageAndAuditMessage(suspensionMessage, nemsMessageId);
        verify(messagePublisher).sendMessage(invalidSuspensionSensitiveTopic, suspensionMessage);
        verify(messagePublisher).sendMessage(invalidSuspensionAuditTopic, stringAuditMessage);
    }

    @Test
    void shouldPublishMessageToBothSensitiveAndAuditTopicAndExtractNemsMessageIdFromTracerIfIsNull() {
        var nemsMessageId = "someId";
        var suspensionMessage = "someString";
        var stringAuditMessage = "{\"nemsMessageId\":\"someId\",\"status\":\"NO_ACTION:INVALID_SUSPENSION\"}";

        when(tracer.getNemsMessageId()).thenReturn(nemsMessageId);

        invalidSuspensionPublisher.sendInvalidMessageAndAuditMessage(suspensionMessage, null);
        verify(messagePublisher).sendMessage(invalidSuspensionSensitiveTopic, suspensionMessage);
        verify(messagePublisher).sendMessage(invalidSuspensionAuditTopic, stringAuditMessage);
    }

    @Test
    void shouldPublishMessageToOnlySensitiveTopicWhenNemsIdIsNullAndTracerReturnsNull() {
        var suspensionMessage = "someString";

        when(tracer.getNemsMessageId()).thenReturn(null);

        invalidSuspensionPublisher.sendInvalidMessageAndAuditMessage(suspensionMessage, null);
        verify(messagePublisher).sendMessage(invalidSuspensionSensitiveTopic, suspensionMessage);
        verifyNoMoreInteractions(messagePublisher);
    }

}