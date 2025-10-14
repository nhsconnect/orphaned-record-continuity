package uk.nhs.prm.repo.suspension.service.suspensionsevents;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.repo.suspension.service.config.MessageProcessProperties;
import uk.nhs.prm.repo.suspension.service.data.LastUpdatedEventService;
import uk.nhs.prm.repo.suspension.service.model.NonSensitiveDataMessage;
import uk.nhs.prm.repo.suspension.service.model.PdsAdaptorSuspensionStatusResponse;
import uk.nhs.prm.repo.suspension.service.pds.InvalidPdsRequestException;
import uk.nhs.prm.repo.suspension.service.pds.PdsService;
import uk.nhs.prm.repo.suspension.service.publishers.MessagePublisherBroker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@ExtendWith(MockitoExtension.class)
public class MessageProcessExecutionTest {

    private MessageProcessExecution messageProcessExecution;

    @Mock
    private MessagePublisherBroker messagePublisherBroker;

    @Mock
    private LastUpdatedEventService lastUpdatedEventService;

    @Mock
    private ManagingOrganisationService managingOrganisationService;

    @Mock
    private MessageProcessProperties config;

    @Mock
    private ConcurrentThreadLock concurrentThreadLock;

    private static final String nemsMessageId = "A6FBE8C3-9144-4DDD-BFFE-B49A96456B29";

    private static final String PREVIOUS_ODS_CODE = "PREVIOUS_ODS_CODE";

    @Mock
    private PdsService pdsService;

    private static final String NHS_NUMBER = "9692294951";

    private static final String LAST_UPDATED_DATE = "2017-11-01T15:00:33+00:00";

    @BeforeEach
    public void setUp() {
        messageProcessExecution = new MessageProcessExecution(messagePublisherBroker,
                pdsService, lastUpdatedEventService, managingOrganisationService, config, new SuspensionEventParser(), concurrentThreadLock);
        setField(messageProcessExecution, "config", config);
    }

    @Test
    void shouldUpdateMofForSyntheticPatientsWhenToggleIsOn() {
        var suspendedMessage = "{\"lastUpdated\":\"2017-11-01T15:00:33+00:00\"," +
                "\"previousOdsCode\":\"PREVIOUS_ODS_CODE\"," +
                "\"eventType\":\"SUSPENSION\"," +
                "\"nhsNumber\":\"9692294951\"," +
                "\"nemsMessageId\":\"" + nemsMessageId + "\"," +
                "\"environment\":\"local\"}";
        setPropertiesWhenProcessOnlySyntheticIsTrue();
        var pdsAdaptorSuspensionStatusResponse
                = new PdsAdaptorSuspensionStatusResponse(NHS_NUMBER, true, null, null, "", false);

        when(pdsService.isSuspended(NHS_NUMBER)).thenReturn(pdsAdaptorSuspensionStatusResponse);

        messageProcessExecution.run(suspendedMessage);

        var suspensionEvent = new SuspensionEvent(NHS_NUMBER, PREVIOUS_ODS_CODE, nemsMessageId, LAST_UPDATED_DATE);

        verify(managingOrganisationService).processMofUpdate(suspendedMessage, suspensionEvent, pdsAdaptorSuspensionStatusResponse);
        verifyNoInteractions(messagePublisherBroker);

        verifyLock(NHS_NUMBER);
    }


    @Test
    void shouldUpdateMofForNonSyntheticPatientsWhenToggleIsOff() {
        var suspendedMessage = "{\"lastUpdated\":\"2017-11-01T15:00:33+00:00\"," +
                "\"previousOdsCode\":\"PREVIOUS_ODS_CODE\"," +
                "\"eventType\":\"SUSPENSION\"," +
                "\"nhsNumber\":\"9692294951\"," +
                "\"nemsMessageId\":\"" + nemsMessageId + "\"," +
                "\"environment\":\"local\"}";

        when(config.getProcessOnlySyntheticPatients()).thenReturn("false");
        var pdsAdaptorSuspensionStatusResponse
                = new PdsAdaptorSuspensionStatusResponse(NHS_NUMBER, true, null, null, "", false);

        when(pdsService.isSuspended(NHS_NUMBER)).thenReturn(pdsAdaptorSuspensionStatusResponse);

        messageProcessExecution.run(suspendedMessage);

        var suspensionEvent = new SuspensionEvent(NHS_NUMBER, PREVIOUS_ODS_CODE, nemsMessageId, LAST_UPDATED_DATE);

        verify(managingOrganisationService).processMofUpdate(suspendedMessage, suspensionEvent, pdsAdaptorSuspensionStatusResponse);
        verifyNoInteractions(messagePublisherBroker);
        verifyLock(NHS_NUMBER);
    }

    @Test
    void shouldUpdateMofForSyntheticPatientsWhenToggleIsOff() {
        var suspendedMessage = "{\"lastUpdated\":\"2017-11-01T15:00:33+00:00\"," +
                "\"previousOdsCode\":\"PREVIOUS_ODS_CODE\"," +
                "\"eventType\":\"SUSPENSION\"," +
                "\"nhsNumber\":\"9692294951\"," +
                "\"nemsMessageId\":\"" + nemsMessageId + "\"," +
                "\"environment\":\"local\"}";
        when(config.getProcessOnlySyntheticPatients()).thenReturn("false");
        var pdsAdaptorSuspensionStatusResponse
                = new PdsAdaptorSuspensionStatusResponse(NHS_NUMBER, true, null, null, "", false);

        when(pdsService.isSuspended(NHS_NUMBER)).thenReturn(pdsAdaptorSuspensionStatusResponse);

        messageProcessExecution.run(suspendedMessage);

        var suspensionEvent = new SuspensionEvent(NHS_NUMBER, PREVIOUS_ODS_CODE, nemsMessageId, LAST_UPDATED_DATE);
        verify(managingOrganisationService).processMofUpdate(suspendedMessage, suspensionEvent, pdsAdaptorSuspensionStatusResponse);
        verifyNoInteractions(messagePublisherBroker);
        verifyLock(NHS_NUMBER);
    }

    @Test
    void shouldNotUpdateMofForNonSyntheticPatientsWhenToggleIsOn() {
        var suspendedMessage = "{\"lastUpdated\":\"2017-11-01T15:00:33+00:00\"," +
                "\"previousOdsCode\":\"PREVIOUS_ODS_CODE\"," +
                "\"eventType\":\"SUSPENSION\"," +
                "\"nhsNumber\":\"9692294951\"," +
                "\"nemsMessageId\":\"" + nemsMessageId + "\"," +
                "\"environment\":\"local\"}";


        when(config.getProcessOnlySyntheticPatients()).thenReturn("true");
        when(config.getSyntheticPatientPrefix()).thenReturn("929");

        var pdsAdaptorSuspensionStatusResponse
                = new PdsAdaptorSuspensionStatusResponse(NHS_NUMBER, true, "A1000", "", "", false);
        when(pdsService.isSuspended(NHS_NUMBER)).thenReturn(pdsAdaptorSuspensionStatusResponse);

        messageProcessExecution.run(suspendedMessage);

        var notSyntheticMessage = new NonSensitiveDataMessage(nemsMessageId, "NO_ACTION:NOT_SYNTHETIC");
        assertEquals("NO_ACTION:NOT_SYNTHETIC", notSyntheticMessage.getStatus());
        verify(pdsService).isSuspended(NHS_NUMBER);
        verify(messagePublisherBroker).notSyntheticMessage(nemsMessageId);
        verifyNoMoreInteractions(pdsService);
        verifyNoMoreInteractions(messagePublisherBroker);
        verifyLock(NHS_NUMBER);
    }
//    @Test
//    void shouldUpdateMofForNonSyntheticPatientsWhoAreSafeListedWhenToggleIsOn() {
//        var suspendedMessageOfRealPatient = "{\"lastUpdated\":\"2017-11-01T15:00:33+00:00\"," +
//                "\"previousOdsCode\":\"PREVIOUS_ODS_CODE\"," +
//                "\"eventType\":\"SUSPENSION\"," +
//                "\"nhsNumber\":\"9692294951\"," +
//                "\"nemsMessageId\":\"" + nemsMessageId + "\"," +
//                "\"environment\":\"local\"}";
//        when(config.getProcessOnlySyntheticPatients()).thenReturn("true");
//        when(config.getSyntheticPatientPrefix()).thenReturn("999");
//        when(config.getAllowedPatientsNhsNumbers()).thenReturn("9692294951,9222294955");
//        var pdsAdaptorSuspensionStatusResponse
//                = new PdsAdaptorSuspensionStatusResponse(NHS_NUMBER, true, null, null, "", false);
//
//        when(pdsService.isSuspended(NHS_NUMBER)).thenReturn(pdsAdaptorSuspensionStatusResponse);
//
//        messageProcessExecution.run(suspendedMessageOfRealPatient);
//
//        var suspensionEvent = new SuspensionEvent(NHS_NUMBER, PREVIOUS_ODS_CODE, nemsMessageId, LAST_UPDATED_DATE);
//
//        verify(managingOrganisationService).processMofUpdate(suspendedMessageOfRealPatient, suspensionEvent, pdsAdaptorSuspensionStatusResponse);
//        verifyNoInteractions(messagePublisherBroker);

//        verifyLock(NHS_NUMBER);

//    }

    @Test
    void shouldUpdateMofIncludingNemsMessageId() {
        var suspendedMessage = "{\"lastUpdated\":\"2017-11-01T15:00:33+00:00\"," +
                "\"previousOdsCode\":\"PREVIOUS_ODS_CODE\"," +
                "\"eventType\":\"SUSPENSION\"," +
                "\"nemsMessageId\":\"" + nemsMessageId + "\"," +
                "\"nhsNumber\":\"9692294951\"," +
                "\"environment\":\"local\"}";
        when(config.getProcessOnlySyntheticPatients()).thenReturn("false");
        var pdsAdaptorSuspensionStatusResponse
                = new PdsAdaptorSuspensionStatusResponse(NHS_NUMBER, true, null, null, "", false);

        when(pdsService.isSuspended(NHS_NUMBER)).thenReturn(pdsAdaptorSuspensionStatusResponse);

        messageProcessExecution.run(suspendedMessage);

        var suspensionEvent = new SuspensionEvent(NHS_NUMBER, PREVIOUS_ODS_CODE, nemsMessageId, LAST_UPDATED_DATE);

        verify(managingOrganisationService).processMofUpdate(suspendedMessage, suspensionEvent, pdsAdaptorSuspensionStatusResponse);
        verifyNoInteractions(messagePublisherBroker);

        verifyLock(NHS_NUMBER);

    }

//    @Test
//    void shouldNotUpdateMofForNonSyntheticPatientsNotInSafeList() {
//        var suspendedMessage = "{\"lastUpdated\":\"2017-11-01T15:00:33+00:00\"," +
//                "\"previousOdsCode\":\"PREVIOUS_ODS_CODE\"," +
//                "\"eventType\":\"SUSPENSION\"," +
//                "\"nhsNumber\":\"9692294951\"," +
//                "\"nemsMessageId\":\"" + nemsMessageId + "\"," +
//                "\"environment\":\"local\"}";
//
//
//        when(config.getProcessOnlySyntheticPatients()).thenReturn("true");
//        when(config.getSyntheticPatientPrefix()).thenReturn("929");
//        when(config.getAllowedPatientsNhsNumbers()).thenReturn("9692294950,9222294955");
//
//        var pdsAdaptorSuspensionStatusResponse
//                = new PdsAdaptorSuspensionStatusResponse(NHS_NUMBER, true, "A1000", "", "", false);
//        when(pdsService.isSuspended(NHS_NUMBER)).thenReturn(pdsAdaptorSuspensionStatusResponse);
//
//        messageProcessExecution.run(suspendedMessage);
//
//        var notSyntheticMessage = new NonSensitiveDataMessage(nemsMessageId, "NO_ACTION:NOT_SYNTHETIC_OR_SAFE_LISTED");
//        assertEquals("NO_ACTION:NOT_SYNTHETIC_OR_SAFE_LISTED", notSyntheticMessage.getStatus());
//        verify(pdsService).isSuspended(NHS_NUMBER);
//        verify(messagePublisherBroker).notSyntheticMessage(nemsMessageId);
//        verifyNoMoreInteractions(pdsService);
//        verifyNoMoreInteractions(messagePublisherBroker);
//        verifyLock(NHS_NUMBER);
//    }

    @Test
    void shouldPublishASuspensionMessageToNotSuspendedSNSTopicWhenPatientIsNotCurrentlySuspended() {
        var notSuspendedMessage = "{\"lastUpdated\":\"2017-11-01T15:00:33+00:00\"," +
                "\"previousOdsCode\":\"B85612\"," +
                "\"eventType\":\"SUSPENSION\"," +
                "\"nhsNumber\":\"9692294951\"," +
                "\"nemsMessageId\":\"" + nemsMessageId + "\"," +
                "\"environment\":\"local\"}";
        var pdsAdaptorSuspensionStatusResponse
                = new PdsAdaptorSuspensionStatusResponse(NHS_NUMBER, false, "null", "", "", false);
        when(pdsService.isSuspended(NHS_NUMBER)).thenReturn(pdsAdaptorSuspensionStatusResponse);

        messageProcessExecution.run(notSuspendedMessage);

        verify(messagePublisherBroker).notSuspendedMessage(nemsMessageId);
        verifyNoMoreInteractions(messagePublisherBroker);
        verifyLock(NHS_NUMBER);
    }

    @Test
    void shouldPublishSuspendedMessageToMofUpdatedSnsTopicWhenPatientIsConfirmedSuspended() {
        var suspendedMessage = "{\"lastUpdated\":\"2017-11-01T15:00:33+00:00\"," +
                "\"previousOdsCode\":\"ORIGINAL_ODS_CODE\"," +
                "\"eventType\":\"SUSPENSION\"," +
                "\"nhsNumber\":\"9692294951\"," +
                "\"nemsMessageId\":\"" + nemsMessageId + "\"," +
                "\"environment\":\"local\"}";
        var pdsAdaptorSuspensionStatusResponse
                = new PdsAdaptorSuspensionStatusResponse(NHS_NUMBER, true, "12345", "NEW_ODS_CODE", "", false);
        when(pdsService.isSuspended(NHS_NUMBER)).thenReturn(pdsAdaptorSuspensionStatusResponse);

        messageProcessExecution.run(suspendedMessage);

        var suspensionEvent = new SuspensionEvent(NHS_NUMBER, "ORIGINAL_ODS_CODE", nemsMessageId, LAST_UPDATED_DATE);
        verify(managingOrganisationService).processMofUpdate(suspendedMessage, suspensionEvent, pdsAdaptorSuspensionStatusResponse);

        verifyNoInteractions(messagePublisherBroker);
    }

    @Test
    void shouldPublishSuspendedMessageToMofUpdatedSnsTopicWhenPatientIsConfirmedSuspendedAndSuperseded() {
        final var ORIGINAL_NHS_NUMBER = NHS_NUMBER;
        final var SUPERSEDED_NHS_NUMBER = "1234567890";

        var suspendedMessage = "{\"lastUpdated\":\"2017-11-01T15:00:33+00:00\"," +
                "\"previousOdsCode\":\"ORIGINAL_ODS_CODE\"," +
                "\"eventType\":\"SUSPENSION\"," +
                "\"nhsNumber\":\"" + ORIGINAL_NHS_NUMBER + "\"," +
                "\"nemsMessageId\":\"" + nemsMessageId + "\"," +
                "\"environment\":\"local\"}";

        var pdsAdaptorSuspensionStatusResponse = new PdsAdaptorSuspensionStatusResponse(SUPERSEDED_NHS_NUMBER, true, "12345", "NEW_ODS_CODE", "ORIGINAL_E_TAG", false);
        var pdsAdaptorSuspensionStatusResponseSuperseded = new PdsAdaptorSuspensionStatusResponse(SUPERSEDED_NHS_NUMBER, true, null, "NEW_ODS_CODE", "SUPERSEDED_E_TAG", false);
        when(pdsService.isSuspended(ORIGINAL_NHS_NUMBER)).thenReturn(pdsAdaptorSuspensionStatusResponse);
        when(pdsService.isSuspended(SUPERSEDED_NHS_NUMBER)).thenReturn(pdsAdaptorSuspensionStatusResponseSuperseded);

        messageProcessExecution.run(suspendedMessage);

        var suspensionEvent = new SuspensionEvent(NHS_NUMBER, "ORIGINAL_ODS_CODE", nemsMessageId, LAST_UPDATED_DATE);

        verify(managingOrganisationService).processMofUpdate(suspendedMessage, suspensionEvent, pdsAdaptorSuspensionStatusResponseSuperseded);
        verifyNoInteractions(messagePublisherBroker);
    }

    @Test
    void shouldPublishSuspendedMessageToMofUpdatedSnsTopicWhenPatientConfirmedSuspendedWhenPdsAlreadyHasADifferentMOFValue() {
        var suspendedMessage = "{\"lastUpdated\":\"2017-11-01T15:00:33+00:00\"," +
                "\"previousOdsCode\":\"LAST_GP_BEFORE_SUSPENSION_ODS_CODE\"," +
                "\"eventType\":\"SUSPENSION\"," +
                "\"nhsNumber\":\"9692294951\"," +
                "\"nemsMessageId\":\"" + nemsMessageId + "\"," +
                "\"environment\":\"local\"}";

        var pdsAdaptorLookUpSuspensionStatusResponse
                = new PdsAdaptorSuspensionStatusResponse(NHS_NUMBER, true, null, "EXISTING_MOF_ODS_CODE", "", false);

        when(pdsService.isSuspended(NHS_NUMBER)).thenReturn(pdsAdaptorLookUpSuspensionStatusResponse);

        messageProcessExecution.run(suspendedMessage);

        var suspensionEvent = new SuspensionEvent(NHS_NUMBER, "LAST_GP_BEFORE_SUSPENSION_ODS_CODE", nemsMessageId, LAST_UPDATED_DATE);
        verify(managingOrganisationService).processMofUpdate(suspendedMessage, suspensionEvent, pdsAdaptorLookUpSuspensionStatusResponse);
        verifyNoInteractions(messagePublisherBroker);
    }


    @Test
    void shouldParsePdsResponseWhenMofFieldNull() {
        String sampleMessage = "{\"lastUpdated\":\"2017-11-01T15:00:33+00:00\"," +
                "\"previousOdsCode\":\"B85612\"," +
                "\"eventType\":\"SUSPENSION\"," +
                "\"nhsNumber\":\"9692294951\"," +
                "\"nemsMessageId\":\"" + nemsMessageId + "\"," +
                "\"environment\":\"local\"}";

        PdsAdaptorSuspensionStatusResponse pdsAdaptorSuspensionStatusResponse
                = new PdsAdaptorSuspensionStatusResponse(NHS_NUMBER, false, "B86041", null, "", false);
        when(pdsService.isSuspended(NHS_NUMBER)).thenReturn(pdsAdaptorSuspensionStatusResponse);
        messageProcessExecution.run(sampleMessage);

        verify(messagePublisherBroker).notSuspendedMessage(nemsMessageId);
        verifyNoMoreInteractions(messagePublisherBroker);
    }

    @Test
    void shouldLandOnDlqWhenSuspensionInvalid() {
        String sampleMessage = "{\"lastUpdated\":\"2017-11-01T15:00:33+00:00\"," +
                "\"previousOdsCode\":\"B85612\"," +
                "\"eventType\":\"SUSPENSION\"," +
                "\"nhsNumber\":\"9692294951\"," +
                "\"nemsMessageId\":\"" + nemsMessageId + "\"," +
                "\"environment\":\"local\"}";
        when(pdsService.isSuspended(NHS_NUMBER)).thenThrow(InvalidPdsRequestException.class);

        Assertions.assertThrows(InvalidPdsRequestException.class, () ->
                messageProcessExecution.run(sampleMessage));

        verify(messagePublisherBroker).invalidMessage(sampleMessage, nemsMessageId);
        verifyNoMoreInteractions(messagePublisherBroker);
    }

    @Test
    void shouldPutDeceasedPatientOnDeceasedPatientSnsTopic() {
        String sampleMessage = "{\"lastUpdated\":\"2017-11-01T15:00:33+00:00\"," +
                "\"previousOdsCode\":\"B85612\"," +
                "\"eventType\":\"SUSPENSION\"," +
                "\"nhsNumber\":\"9692294951\"," +
                "\"nemsMessageId\":\"" + nemsMessageId + "\"," +
                "\"environment\":\"local\"}";

        when(pdsService.isSuspended(NHS_NUMBER))
                .thenReturn(new PdsAdaptorSuspensionStatusResponse(NHS_NUMBER, null, null, null, "W/6", true));

        messageProcessExecution.run(sampleMessage);

        verify(messagePublisherBroker).deceasedPatientMessage(nemsMessageId);
        verifyNoMoreInteractions(messagePublisherBroker);
    }

    @Test
    void shouldSendToInvalidSuspensionWhenCannotParseSuspensionMessage() {
        String sampleMessage = "bad_message";

        Assertions.assertThrows(InvalidSuspensionMessageException.class, () ->
                messageProcessExecution.run(sampleMessage));

        verify(messagePublisherBroker).invalidMessage(sampleMessage, null);
        verifyNoMoreInteractions(messagePublisherBroker);
    }

    private void setPropertiesWhenProcessOnlySyntheticIsTrue() {
        when(config.getProcessOnlySyntheticPatients()).thenReturn("true");
        when(config.getSyntheticPatientPrefix()).thenReturn("969");
    }

    private void verifyLock(String nhsNumber) {
        verify(concurrentThreadLock).lock(nhsNumber);
        verify(concurrentThreadLock).unlock(nhsNumber);
    }
} 
