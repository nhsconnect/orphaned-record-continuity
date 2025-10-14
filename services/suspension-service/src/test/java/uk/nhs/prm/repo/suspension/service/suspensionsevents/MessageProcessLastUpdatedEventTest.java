package uk.nhs.prm.repo.suspension.service.suspensionsevents;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.repo.suspension.service.config.MessageProcessProperties;
import uk.nhs.prm.repo.suspension.service.data.LastUpdatedEventService;
import uk.nhs.prm.repo.suspension.service.model.PdsAdaptorSuspensionStatusResponse;
import uk.nhs.prm.repo.suspension.service.pds.PdsService;
import uk.nhs.prm.repo.suspension.service.publishers.*;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MessageProcessLastUpdatedEventTest {
    private MessageProcessExecution messageProcessExecution;

    @Mock
    private MessagePublisherBroker messagePublisherBroker;
    @Mock
    private LastUpdatedEventService lastUpdatedEventService;
    @Mock
    private ManagingOrganisationService managingOrganisationService;
    @Mock
    private PdsService pdsService;
    @Mock
    private MessageProcessProperties config;
    @Mock
    private ConcurrentThreadLock concurrentThreadLock;

    private static final String nemsMessageId = "A6FBE8C3-9144-4DDD-BFFE-B49A96456B29";
    private static final String nhsNumber = "9692294951";
    private static final String lastUpdated = "2017-11-01T15:00:33+00:00";
    private static final String suspendedMessage = "{\"lastUpdated\":\"" + lastUpdated + "\"," +
            "\"previousOdsCode\":\"PREVIOUS_ODS_CODE\"," +
            "\"eventType\":\"SUSPENSION\"," +
            "\"nhsNumber\":\"" + nhsNumber + "\"," +
            "\"nemsMessageId\":\"" + nemsMessageId + "\"," +
            "\"environment\":\"local\"}";

    @BeforeEach
    public void setUp() {
        messageProcessExecution = new MessageProcessExecution(messagePublisherBroker,
                pdsService, lastUpdatedEventService, managingOrganisationService,config, new SuspensionEventParser(), concurrentThreadLock);
    }

    @Test
    void shouldSendOutOfOrderNemsMessagesToEventOutOrderQueue() {
        when(lastUpdatedEventService.isOutOfOrder(nhsNumber, lastUpdated)).thenReturn(true);

        messageProcessExecution.run(suspendedMessage);

        verify(lastUpdatedEventService).isOutOfOrder(nhsNumber, lastUpdated);
        verify(messagePublisherBroker).eventOutOfOrderMessage(nemsMessageId);
        verifyNoMoreInteractions(messagePublisherBroker);
        verifyNoMoreInteractions(managingOrganisationService);
    }

    @Test
    void shouldSaveRecordIfisNotOutOfOrder() {
        var pdsAdaptorSuspensionStatusResponse = mockMofDependencies();
        when(lastUpdatedEventService.isOutOfOrder(nhsNumber, lastUpdated)).thenReturn(false);

        messageProcessExecution.run(suspendedMessage);

        var suspensionEvent = new SuspensionEvent(nhsNumber, "PREVIOUS_ODS_CODE", nemsMessageId, "2017-11-01T15:00:33+00:00");

        verify(lastUpdatedEventService).isOutOfOrder(nhsNumber, lastUpdated);
        verify(lastUpdatedEventService).save(nhsNumber, lastUpdated);
        verify(managingOrganisationService).processMofUpdate(suspendedMessage, suspensionEvent, pdsAdaptorSuspensionStatusResponse);

    }

    private PdsAdaptorSuspensionStatusResponse mockMofDependencies() {
        var pdsAdaptorSuspensionStatusResponse
                = new PdsAdaptorSuspensionStatusResponse(nhsNumber, true, null, null, "", false);

        when(pdsService.isSuspended(nhsNumber)).thenReturn(pdsAdaptorSuspensionStatusResponse);
        return pdsAdaptorSuspensionStatusResponse;
    }
}
