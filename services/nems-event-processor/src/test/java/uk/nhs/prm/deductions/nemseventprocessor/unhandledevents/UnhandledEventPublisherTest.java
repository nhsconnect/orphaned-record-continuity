package uk.nhs.prm.deductions.nemseventprocessor.unhandledevents;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.deductions.nemseventprocessor.MessagePublisher;
import uk.nhs.prm.deductions.nemseventprocessor.nemsevents.NonSuspendedMessage;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static uk.nhs.prm.deductions.nemseventprocessor.audit.AuditMessageStatus.NO_ACTION_NON_SUSPENSION;

@ExtendWith(MockitoExtension.class)
class UnhandledEventPublisherTest {

    @Mock
    private MessagePublisher messagePublisher;

    private final static String unhandledTopicArn = "unhandledTopicArn";

    private UnhandledEventPublisher unhandledEventPublisher;

    @BeforeEach
    void setUp() {
        unhandledEventPublisher = new UnhandledEventPublisher(messagePublisher, unhandledTopicArn);
    }

    @Test
    void shouldPublishMessageToTheUnhandledTopic() {
        NonSuspendedMessage nonSuspendedMessage = new NonSuspendedMessage("someId", NO_ACTION_NON_SUSPENSION);
        String jsonMessage = "{\"nemsMessageId\":\"someId\",\"messageStatus\":\"NO_ACTION:NON_SUSPENSION\"}";
        unhandledEventPublisher.sendMessage(nonSuspendedMessage, "some-reason");
        verify(messagePublisher).sendMessage(eq(unhandledTopicArn), eq(jsonMessage), anyString(), anyString());
    }

    @Test
    void shouldProvideReasonMessageIsUnhandledAsMetaData() {
        NonSuspendedMessage nonSuspendedMessage = new NonSuspendedMessage("someId", NO_ACTION_NON_SUSPENSION);
        String jsonMessage = "{\"nemsMessageId\":\"someId\",\"messageStatus\":\"NO_ACTION:NON_SUSPENSION\"}";
        String reasonUnhandled = "failedToParse";
        unhandledEventPublisher.sendMessage(nonSuspendedMessage, reasonUnhandled);
        verify(messagePublisher).sendMessage(unhandledTopicArn, jsonMessage, "reasonUnhandled", reasonUnhandled);
    }
}
