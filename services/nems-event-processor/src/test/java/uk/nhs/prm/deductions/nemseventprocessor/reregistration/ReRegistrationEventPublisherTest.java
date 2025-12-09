package uk.nhs.prm.deductions.nemseventprocessor.reregistration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.deductions.nemseventprocessor.MessagePublisher;
import uk.nhs.prm.deductions.nemseventprocessor.nemsevents.NemsEventMessage;
import uk.nhs.prm.deductions.nemseventprocessor.suspensions.SuspensionsEventPublisher;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReRegistrationEventPublisherTest {

    @Mock
    private MessagePublisher messagePublisher;

    private final static String reRegistrationTopicArn = "reRegistrationTopicArn";

    private ReRegistrationEventPublisher reRegistrationEventPublisher;

    @BeforeEach
    void setUp() {
        reRegistrationEventPublisher = new ReRegistrationEventPublisher(messagePublisher, reRegistrationTopicArn);
    }

    @Test
    void shouldPublishMessageToTheReRegistrationTopic() {
        var reRegistrationEvent = new ReRegistrationEvent("some-nhs-number", "some-ods-code", "some-nems-message-id", "last-updated");
        reRegistrationEventPublisher.sendMessage(reRegistrationEvent);

        verify(messagePublisher).sendMessage(reRegistrationTopicArn, reRegistrationEvent.toJsonString(), "nemsMessageId", "some-nems-message-id");
    }
}