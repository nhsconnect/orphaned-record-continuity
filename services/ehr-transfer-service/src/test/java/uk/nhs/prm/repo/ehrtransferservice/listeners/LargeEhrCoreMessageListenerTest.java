package uk.nhs.prm.repo.ehrtransferservice.listeners;

import com.amazon.sqs.javamessaging.message.SQSTextMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.repo.ehrtransferservice.logging.Tracer;
import uk.nhs.prm.repo.ehrtransferservice.exceptions.DuplicateMessageException;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.ParsedMessage;
import uk.nhs.prm.repo.ehrtransferservice.handlers.MessageHandler;
import uk.nhs.prm.repo.ehrtransferservice.models.LargeSqsMessage;
import uk.nhs.prm.repo.ehrtransferservice.parsers.S3ExtendedMessageFetcher;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LargeEhrCoreMessageListenerTest {

    @Mock
    Tracer tracer;

    @Mock
    S3ExtendedMessageFetcher extendedMessageFetcher;

    @Mock
    MessageHandler<ParsedMessage> largeEhrCoreMessageHandler;

    @InjectMocks
    S3ExtendedMessageListener largeEhrCoreMessageListener;

    @Test
    void shouldUpdateTraceIdFromSqsAttributes() throws Exception {
        SQSTextMessage message = getSqsTextMessage();
        largeEhrCoreMessageListener.onMessage(message);
        verify(tracer).setMDCContextFromSqs(message);
    }

    @Test
    void shouldPassTheMessageToTheExtendedMessageFetcher() throws Exception {
        var message = spy(new SQSTextMessage("payload"));

        largeEhrCoreMessageListener.onMessage(message);

        verify(extendedMessageFetcher).fetchAndParse(message);
    }

    @Test
    void shouldCallLargeEhrCoreMessageHandlerWithTheExtendedMessageFromTheFetcher() throws Exception {
        var message = getSqsTextMessage();
        var largeSqsMessage = mock(LargeSqsMessage.class);

        when(extendedMessageFetcher.fetchAndParse(message)).thenReturn(largeSqsMessage);

        largeEhrCoreMessageListener.onMessage(message);

        verify(largeEhrCoreMessageHandler).handleMessage(largeSqsMessage);
    }

    @Test
    void shouldAcknowledgeMessageWhenDuplicateMessageErrorIsCaught() throws Exception {
        var message = getSqsTextMessage();
        var largeSqsMessage = mock(LargeSqsMessage.class);

        when(extendedMessageFetcher.fetchAndParse(message)).thenReturn(largeSqsMessage);
        doThrow(DuplicateMessageException.class).when(largeEhrCoreMessageHandler).handleMessage(any());

        largeEhrCoreMessageListener.onMessage(message);

        verify(message, times(1)).acknowledge();
    }

    private SQSTextMessage getSqsTextMessage() throws Exception {
        String payload = "payload";
        return spy(new SQSTextMessage(payload));
    }
}
