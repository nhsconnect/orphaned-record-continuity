package uk.nhs.prm.deductions.nemseventprocessor.dlq;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.deductions.nemseventprocessor.MessagePublisher;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DeadLetterQueuePublisherTest {
    @Mock
    private MessagePublisher messagePublisher;

    private final static String deadLetterQueueTopicArn = "deadLetterQueueTopicArn";
    private DeadLetterQueuePublisher deadLetterQueuePublisher;

    @BeforeEach
    void setUp() {
        deadLetterQueuePublisher = new DeadLetterQueuePublisher(messagePublisher, deadLetterQueueTopicArn);
    }

    @Test
    void shouldPublishMessageToTheDeadLetterTopic() {
        deadLetterQueuePublisher.sendMessage("message", "can't process");
        verify(messagePublisher).sendMessage(deadLetterQueueTopicArn, "message", "reasonCannotProcess", "can't process");
    }
}