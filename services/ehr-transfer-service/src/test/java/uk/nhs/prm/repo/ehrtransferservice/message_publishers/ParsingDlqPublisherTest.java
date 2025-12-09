package uk.nhs.prm.repo.ehrtransferservice.message_publishers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ParsingDlqPublisherTest {
    @Mock
    private MessagePublisher messagePublisher;

    private final static String topicArn = "topicArn";

    private ParsingDlqPublisher parsingDlqPublisher;

    @BeforeEach
    void setUp() {
        parsingDlqPublisher = new ParsingDlqPublisher(messagePublisher, topicArn);
    }

    @Test
    void shouldPublishMessageToTheParsingDlqTopic() {
        parsingDlqPublisher.sendMessage("message");
        verify(messagePublisher).sendMessage(topicArn, "message");
    }
}