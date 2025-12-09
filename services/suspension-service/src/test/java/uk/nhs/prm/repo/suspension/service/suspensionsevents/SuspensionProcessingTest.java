package uk.nhs.prm.repo.suspension.service.suspensionsevents;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.IThrowableProxy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.repo.suspension.service.config.MessageProcessProperties;
import uk.nhs.prm.repo.suspension.service.data.LastUpdatedEventService;
import uk.nhs.prm.repo.suspension.service.model.PdsAdaptorSuspensionStatusResponse;
import uk.nhs.prm.repo.suspension.service.pds.IntermittentErrorPdsException;
import uk.nhs.prm.repo.suspension.service.pds.InvalidPdsRequestException;
import uk.nhs.prm.repo.suspension.service.pds.PdsService;
import uk.nhs.prm.repo.suspension.service.publishers.MessagePublisherBroker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static uk.nhs.prm.repo.suspension.service.logging.TestLogAppender.addTestLogAppender;

@ExtendWith(MockitoExtension.class)
public class SuspensionProcessingTest {

    private SuspensionMessageProcessor suspensionMessageProcessor;

    private MessageProcessExecution messageProcessExecution;

    @Mock
    private MessagePublisherBroker messagePublisherBroker;

    @Mock
    private ManagingOrganisationService managingOrganisationService;

    @Mock
    private LastUpdatedEventService lastUpdatedEventService;
    @Mock
    private MessageProcessProperties config;

    @Mock
    private ConcurrentThreadLock concurrentThreadLock;

    private static final String nemsMessageId = "A6FBE8C3-9144-4DDD-BFFE-B49A96456B29";

    @Mock
    private PdsService pdsService;

    private static final String NHS_NUMBER = "9692294951";

    @BeforeEach
    public void setUp() {
        messageProcessExecution = new MessageProcessExecution(messagePublisherBroker,
                pdsService, lastUpdatedEventService, managingOrganisationService,config, new SuspensionEventParser(), concurrentThreadLock);
        suspensionMessageProcessor = new SuspensionMessageProcessor(messageProcessExecution);
        setField(suspensionMessageProcessor, "initialIntervalMillis", 1);
        setField(suspensionMessageProcessor, "maxAttempts", 5);
        setField(suspensionMessageProcessor, "multiplier", 2.0);
    }

    @Test
    void shouldRetryUpToFiveTimes() {
        var sampleMessage = "{\"lastUpdated\":\"2017-11-01T15:00:33+00:00\"," +
                "\"previousOdsCode\":\"B85612\"," +
                "\"eventType\":\"SUSPENSION\"," +
                "\"nhsNumber\":\"9692294951\"," +
                "\"environment\":\"local\"}";

        when(pdsService.isSuspended(NHS_NUMBER)).thenThrow(IntermittentErrorPdsException.class);


        assertThrows(IntermittentErrorPdsException.class, () ->
                suspensionMessageProcessor.process(sampleMessage));

        int numberOfInvocations = 5;

        verify(pdsService, times(numberOfInvocations)).isSuspended(NHS_NUMBER);

    }

    @Test
    void shouldNotRetryIfDoNotGetException() {
        var sampleMessage = "{\"lastUpdated\":\"2017-11-01T15:00:33+00:00\"," +
                "\"previousOdsCode\":\"B85612\"," +
                "\"eventType\":\"SUSPENSION\"," +
                "\"nhsNumber\":\"9692294951\"," +
                "\"nemsMessageId\":\"" + nemsMessageId + "\"," +
                "\"environment\":\"local\"}";

        when(pdsService.isSuspended(NHS_NUMBER)).thenThrow(IntermittentErrorPdsException.class)
                .thenReturn(new PdsAdaptorSuspensionStatusResponse(NHS_NUMBER, false, null, null, "", false));

        suspensionMessageProcessor.process(sampleMessage);

        int numberOfInvocations = 2;

        verify(pdsService, times(numberOfInvocations)).isSuspended(NHS_NUMBER);
    }

    @Test
    void shouldNotRetryWhenGotInvalidPdsRequestException() {
        var sampleMessage = createSuspensionMessage(NHS_NUMBER);

        when(pdsService.isSuspended(NHS_NUMBER)).thenThrow(InvalidPdsRequestException.class);

        assertThrows(InvalidPdsRequestException.class, () ->
                suspensionMessageProcessor.process(sampleMessage));

        verify(pdsService, times(1)).isSuspended(NHS_NUMBER);
    }

    @Test
    void shouldLogExceptionsThatAreRetriedAtInfoLevel() {
        var logged = addTestLogAppender();

        var cause = new IntermittentErrorPdsException("some retryable exception", new IllegalArgumentException());
        when(pdsService.isSuspended(NHS_NUMBER)).thenThrow(cause);

        assertThrows(Exception.class, () ->
            suspensionMessageProcessor.process(createSuspensionMessage(NHS_NUMBER)));

        var logline = logged.findLoggedEvent("Caught retryable exception");
        assertThat(logline).isNotNull();
        assertThat(logline.getLevel().toString()).isEqualTo("INFO");

        var loggedException = logline.getThrowableProxy();
        assertThat(loggedException.getMessage()).isEqualTo("some retryable exception");
        assertThat(loggedException.getCause().getClassName()).contains("IllegalArgumentException");
    }

    @Test
    void shouldAlsoLogUncaughtExceptionsThatAreNotRetriedButAtErrorLevel() {
        var logged = addTestLogAppender();

        var cause = new IllegalArgumentException("some uncaught exception");

        when(pdsService.isSuspended(NHS_NUMBER)).thenThrow(cause);

        assertThrows(Exception.class, () ->
                suspensionMessageProcessor.process(createSuspensionMessage(NHS_NUMBER)));

        var logline = logged.findLoggedEvent("Uncaught exception");
        assertThat(logline).isNotNull();
        assertThat(logline.getLevel().toString()).isEqualTo("ERROR");

        var loggedException = logline.getThrowableProxy();
        assertThat(loggedException.getMessage()).isEqualTo("some uncaught exception");
    }

    @Test
    void shouldNotProcessMessagesWhichAreNotInCorrectFormat() {
        var message = "invalid message";
        assertThrows(Exception.class, () -> suspensionMessageProcessor.process(message));
    }

    private String createSuspensionMessage(String nhsNumber) {
        return "{\"lastUpdated\":\"2017-11-01T15:00:33+00:00\"," +
                "\"previousOdsCode\":\"B85612\"," +
                "\"eventType\":\"SUSPENSION\"," +
                "\"nhsNumber\":\"" + nhsNumber + "\"," +
                "\"nemsMessageId\":\"A6FBE8C3-9144-4DDD-BFFE-B49A96456B29\"," +
                "\"environment\":\"local\"}";
    }
}