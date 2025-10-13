package uk.nhs.prm.repo.ehrtransferservice.listeners;

import com.amazon.sqs.javamessaging.message.SQSTextMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.repo.ehrtransferservice.logging.Tracer;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.ParsedMessage;
import uk.nhs.prm.repo.ehrtransferservice.handlers.SmallEhrMessageHandler;
import uk.nhs.prm.repo.ehrtransferservice.parsers.Parser;
import uk.nhs.prm.repo.ehrtransferservice.parsers.S3ExtendedMessageFetcher;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SmallEhrListenerTest {

    @Mock
    Tracer tracer;

    @Mock
    Parser parser;

    @Mock
    S3ExtendedMessageFetcher extendedMessageFetcher;

    @Mock
    SmallEhrMessageHandler smallEhrMessageHandler;

    @InjectMocks
    S3ExtendedMessageListener smallEhrMessageListener;

    public SmallEhrListenerTest() {
        parser = new Parser();
    }

    @Test
    void shouldUpdateTraceIdFromSqsAttributes() throws Exception {
        var message = spy(new SQSTextMessage("payload"));

        smallEhrMessageListener.onMessage(message);

        verify(tracer).setMDCContextFromSqs(message);
    }

    @Test
    void shouldPassParsedMessageFromTheExtendedMessageFetcherToHandlerAndAcknowledgeIt() throws Exception {
        var message = spy(new SQSTextMessage("payload"));
        var parsedMessage = new StubParsedMessage();
        when(extendedMessageFetcher.fetchAndParse(message)).thenReturn(parsedMessage);

        smallEhrMessageListener.onMessage(message);

        verify(extendedMessageFetcher).fetchAndParse(message);
        verify(tracer).setMDCContextFromSqs(message);
        verify(message, times(1)).acknowledge();
        verify(smallEhrMessageHandler).handleMessage(parsedMessage);
    }

    @Test
    void shouldNotAcknowledgeMessageWhenAnExceptionOccursInFetchingOrParsing() throws Exception {
        var message = spy(new SQSTextMessage("bleuch"));

        when(extendedMessageFetcher.fetchAndParse(message)).thenThrow(new IllegalArgumentException());

        smallEhrMessageListener.onMessage(message);

        verify(message, never()).acknowledge();
    }

    @Test
    void shouldNotAcknowledgeMessageWhenAnExceptionOccursInHandling() throws Exception {
        var message = spy(new SQSTextMessage("boom"));

        doThrow(new IllegalArgumentException()).when(smallEhrMessageHandler).handleMessage(any());

        smallEhrMessageListener.onMessage(message);

        verify(message, never()).acknowledge();
    }

    class StubParsedMessage extends ParsedMessage {
        public StubParsedMessage() {
            super(null, null, null);
        }
    }
}