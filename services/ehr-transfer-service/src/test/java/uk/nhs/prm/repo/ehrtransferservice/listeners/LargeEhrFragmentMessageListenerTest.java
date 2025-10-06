package uk.nhs.prm.repo.ehrtransferservice.listeners;

import com.amazon.sqs.javamessaging.message.SQSTextMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.repo.ehrtransferservice.logging.Tracer;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.ParsedMessage;
import uk.nhs.prm.repo.ehrtransferservice.handlers.MessageHandler;
import uk.nhs.prm.repo.ehrtransferservice.parsers.S3ExtendedMessageFetcher;
import uk.nhs.prm.repo.ehrtransferservice.models.LargeSqsMessage;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LargeEhrFragmentMessageListenerTest {

    @Mock
    Tracer tracer;

    @Mock
    S3ExtendedMessageFetcher s3ExtendedMessageFetcher;

    @Mock
    MessageHandler<ParsedMessage> largeMessageFragmentHandler;

    private static final String payload = "payload";

    @InjectMocks
    S3ExtendedMessageListener largeMessageFragmentsListener;

    @Test
    void shouldSetTraceIdWhenReceivingLargeMessageFragment() throws Exception {
        var sqsTextMessage = getSqsTextMessage();
        largeMessageFragmentsListener.onMessage(sqsTextMessage);
        verify(tracer).setMDCContextFromSqs(sqsTextMessage);
    }

    @Test
    void shouldCallLargeEhrSqsServiceWithTheMessagePayload() throws Exception {
        var message = getSqsTextMessage();
        largeMessageFragmentsListener.onMessage(message);
        verify(s3ExtendedMessageFetcher).fetchAndParse(message);
    }

    @Test
    void shouldCallLargeMessageFragmentHandlerWithLargeSqsPayload() throws Exception {
        var message = getSqsTextMessage();
        var largeSqsMessageMock = mock(LargeSqsMessage.class);
        when(s3ExtendedMessageFetcher.fetchAndParse(message)).thenReturn(largeSqsMessageMock);
        largeMessageFragmentsListener.onMessage(message);
        verify(largeMessageFragmentHandler).handleMessage(largeSqsMessageMock);
    }

    private SQSTextMessage getSqsTextMessage() throws Exception {
        return spy(new SQSTextMessage(payload));
    }
}
