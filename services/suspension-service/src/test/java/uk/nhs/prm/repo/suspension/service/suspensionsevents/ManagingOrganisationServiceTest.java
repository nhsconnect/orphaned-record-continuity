package uk.nhs.prm.repo.suspension.service.suspensionsevents;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.repo.suspension.service.config.ToggleConfig;
import uk.nhs.prm.repo.suspension.service.model.PdsAdaptorSuspensionStatusResponse;
import uk.nhs.prm.repo.suspension.service.pds.InvalidPdsRequestException;
import uk.nhs.prm.repo.suspension.service.pds.PdsService;
import uk.nhs.prm.repo.suspension.service.publishers.MessagePublisherBroker;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ManagingOrganisationServiceTest {

    @Mock
    private PdsService pdsService;

    @Mock
    private MessagePublisherBroker messagePublisherBroker;

    @Mock
    private ToggleConfig toggleConfig;


    private ManagingOrganisationService mofService;

    private final static String STRING_SUSPENSION_MESSAGE = "some-original-message";
    private final static String NHS_NUMBER = "1234567890";
    private final static String PREVIOUS_ODS_CODE = "A1000";
    private static final String SAFE_LISTED_ODS_CODE = "ODS123";
    private final static String NEMS_MESSAGE_ID = "Aasdfgfhghgfg";
    private final static String LAST_UPDATED_DATE = "2017-11-01T15:00:33+00:00";
    private final static String RECORD_E_TAG = "E1";
    private final static String REPO_ODS_CODE = "R1234";
    private final static String ALLOWED_ODS_CODES = "ODS123,ods456";

    @BeforeEach
    void setUp() {
        mofService = new ManagingOrganisationService(pdsService, messagePublisherBroker, REPO_ODS_CODE, toggleConfig, ALLOWED_ODS_CODES);
    }

    @Test
    void shouldSendMofUpdateAndActiveSuspensionMessageForSuspendedPatient() {
        var suspensionEvent = new SuspensionEvent(NHS_NUMBER, PREVIOUS_ODS_CODE, NEMS_MESSAGE_ID, LAST_UPDATED_DATE);
        var beforeUpdateResponse = new PdsAdaptorSuspensionStatusResponse(NHS_NUMBER, true, null,
                null, RECORD_E_TAG, false);

        var afterUpdateResponse = new PdsAdaptorSuspensionStatusResponse(NHS_NUMBER, true, null,
                PREVIOUS_ODS_CODE, "E2", false);

        when(pdsService.updateMof(NHS_NUMBER, PREVIOUS_ODS_CODE, RECORD_E_TAG)).thenReturn(afterUpdateResponse);

        mofService.processMofUpdate(STRING_SUSPENSION_MESSAGE, suspensionEvent, beforeUpdateResponse);

        verify(pdsService).updateMof(NHS_NUMBER, PREVIOUS_ODS_CODE, RECORD_E_TAG);
        verify(messagePublisherBroker).mofUpdatedMessage(NEMS_MESSAGE_ID, PREVIOUS_ODS_CODE, false);
        //verify(messagePublisherBroker).activeSuspensionMessage(NHS_NUMBER, PREVIOUS_ODS_CODE, LAST_UPDATED_DATE);
    }

    @Test
    void shouldSendMofUpdateForSuspendedPatientWhenCurrentOdsCodeIsDifferentValue() {
        var suspensionEvent = new SuspensionEvent(NHS_NUMBER, PREVIOUS_ODS_CODE, NEMS_MESSAGE_ID, LAST_UPDATED_DATE);
        var beforeUpdateResponse = new PdsAdaptorSuspensionStatusResponse(NHS_NUMBER, true, "A1000",
                null, RECORD_E_TAG, false);

        var afterUpdateResponse = new PdsAdaptorSuspensionStatusResponse(NHS_NUMBER, true, null,
                PREVIOUS_ODS_CODE, "E2", false);

        when(pdsService.updateMof(NHS_NUMBER, PREVIOUS_ODS_CODE, RECORD_E_TAG)).thenReturn(afterUpdateResponse);

        mofService.processMofUpdate(STRING_SUSPENSION_MESSAGE, suspensionEvent, beforeUpdateResponse);

        verify(pdsService).updateMof(NHS_NUMBER, PREVIOUS_ODS_CODE, RECORD_E_TAG);
        verify(messagePublisherBroker).mofUpdatedMessage(NEMS_MESSAGE_ID, PREVIOUS_ODS_CODE, false);
    }

    @Test
    void shouldSendMofUpdateForSuspendedPatientWhenSuperseded() {
        var suspensionEvent = new SuspensionEvent(NHS_NUMBER, PREVIOUS_ODS_CODE, NEMS_MESSAGE_ID, LAST_UPDATED_DATE);
        var supersededNhsNumber = "different-nhs-number";
        var beforeUpdateResponse = new PdsAdaptorSuspensionStatusResponse(supersededNhsNumber, true, null,
                null, RECORD_E_TAG, false);

        var afterUpdateResponse = new PdsAdaptorSuspensionStatusResponse(supersededNhsNumber, true, null,
                PREVIOUS_ODS_CODE, "E2", false);

        when(pdsService.updateMof(supersededNhsNumber, PREVIOUS_ODS_CODE, RECORD_E_TAG)).thenReturn(afterUpdateResponse);

        mofService.processMofUpdate(STRING_SUSPENSION_MESSAGE, suspensionEvent, beforeUpdateResponse);

        verify(pdsService).updateMof(supersededNhsNumber, PREVIOUS_ODS_CODE, RECORD_E_TAG);
        verify(messagePublisherBroker).mofUpdatedMessage(NEMS_MESSAGE_ID, PREVIOUS_ODS_CODE, true);
    }

    @Test
    void shouldSendMofNotUpdateWhenMofTheSameAsPreviousGp() {
        var suspensionEvent = new SuspensionEvent(NHS_NUMBER, PREVIOUS_ODS_CODE, NEMS_MESSAGE_ID, LAST_UPDATED_DATE);
        var beforeUpdateResponse = new PdsAdaptorSuspensionStatusResponse(NHS_NUMBER, true, null,
                PREVIOUS_ODS_CODE, RECORD_E_TAG, false);


        mofService.processMofUpdate(STRING_SUSPENSION_MESSAGE, suspensionEvent, beforeUpdateResponse);

        verify(messagePublisherBroker).mofNotUpdatedMessage(NEMS_MESSAGE_ID, false);
        verifyNoInteractions(pdsService);
    }

    @Test
    void shouldThrowInvalidRequestExceptionIfPdsFails() {
        var suspensionEvent = new SuspensionEvent(NHS_NUMBER, PREVIOUS_ODS_CODE, NEMS_MESSAGE_ID, LAST_UPDATED_DATE);
        var beforeUpdateResponse = new PdsAdaptorSuspensionStatusResponse(NHS_NUMBER, true, null,
                null, RECORD_E_TAG, false);

        when(pdsService.updateMof(any(), any(), any())).thenThrow(InvalidPdsRequestException.class);

        Assertions.assertThrows(InvalidPdsRequestException.class, () ->
                mofService.processMofUpdate(STRING_SUSPENSION_MESSAGE, suspensionEvent, beforeUpdateResponse));

        verify(messagePublisherBroker).invalidMessage(STRING_SUSPENSION_MESSAGE, NEMS_MESSAGE_ID);
    }

    @Test
    void shouldSendMofUpdateToRepoWhenToggleIsTrue() {
        var suspensionEvent = new SuspensionEvent(NHS_NUMBER, PREVIOUS_ODS_CODE, NEMS_MESSAGE_ID, LAST_UPDATED_DATE);
        var beforeUpdateResponse = new PdsAdaptorSuspensionStatusResponse(NHS_NUMBER, true, null,
                null, RECORD_E_TAG, false);

        var afterUpdateResponse = new PdsAdaptorSuspensionStatusResponse(NHS_NUMBER, true, null,
                REPO_ODS_CODE, "E2", false);


        when(toggleConfig.isCanUpdateManagingOrganisationToRepo()).thenReturn(true);
        when(toggleConfig.isRepoProcessOnlySafeListedOdsCodes()).thenReturn(false);
        when(pdsService.updateMof(NHS_NUMBER, REPO_ODS_CODE, RECORD_E_TAG)).thenReturn(afterUpdateResponse);

        mofService.processMofUpdate(STRING_SUSPENSION_MESSAGE, suspensionEvent, beforeUpdateResponse);

        verify(pdsService).updateMof(NHS_NUMBER, REPO_ODS_CODE, RECORD_E_TAG);
        verify(messagePublisherBroker).repoIncomingMessage(afterUpdateResponse, suspensionEvent);
    }

    @Test
    void shouldSendMofUpdateToRepoWhenBothTogglesAreTrueAndOdsCodeIsSafeListed() {
        var suspensionEvent = new SuspensionEvent(NHS_NUMBER, SAFE_LISTED_ODS_CODE, NEMS_MESSAGE_ID, LAST_UPDATED_DATE);
        var beforeUpdateResponse = new PdsAdaptorSuspensionStatusResponse(NHS_NUMBER, true, null,
                null, RECORD_E_TAG, false);

        var afterUpdateResponse = new PdsAdaptorSuspensionStatusResponse(NHS_NUMBER, true, null,
                REPO_ODS_CODE, "E2", false);


        when(toggleConfig.isCanUpdateManagingOrganisationToRepo()).thenReturn(true);
        when(toggleConfig.isRepoProcessOnlySafeListedOdsCodes()).thenReturn(true);
        when(pdsService.updateMof(NHS_NUMBER, REPO_ODS_CODE, RECORD_E_TAG)).thenReturn(afterUpdateResponse);

        mofService.processMofUpdate(STRING_SUSPENSION_MESSAGE, suspensionEvent, beforeUpdateResponse);

        verify(pdsService).updateMof(NHS_NUMBER, REPO_ODS_CODE, RECORD_E_TAG);
        verify(messagePublisherBroker).repoIncomingMessage(afterUpdateResponse, suspensionEvent);
    }

    @Test
    void shouldSendMofUpdateToPreviousGpWhenBothTogglesAreTrueAndOdsCodeIsNotInTheSafeList() {
        var suspensionEvent = new SuspensionEvent(NHS_NUMBER, PREVIOUS_ODS_CODE, NEMS_MESSAGE_ID, LAST_UPDATED_DATE);
        var beforeUpdateResponse = new PdsAdaptorSuspensionStatusResponse(NHS_NUMBER, true, null,
                null, RECORD_E_TAG, false);

        var afterUpdateResponse = new PdsAdaptorSuspensionStatusResponse(NHS_NUMBER, true, null,
                PREVIOUS_ODS_CODE, "E2", false);


        when(toggleConfig.isCanUpdateManagingOrganisationToRepo()).thenReturn(true);
        when(toggleConfig.isRepoProcessOnlySafeListedOdsCodes()).thenReturn(true);
        when(pdsService.updateMof(NHS_NUMBER, PREVIOUS_ODS_CODE, RECORD_E_TAG)).thenReturn(afterUpdateResponse);

        mofService.processMofUpdate(STRING_SUSPENSION_MESSAGE, suspensionEvent, beforeUpdateResponse);

        verify(pdsService).updateMof(NHS_NUMBER, PREVIOUS_ODS_CODE, RECORD_E_TAG);
        verify(messagePublisherBroker).mofUpdatedMessage(NEMS_MESSAGE_ID, PREVIOUS_ODS_CODE, false);
    }

    @Test
    void shouldSendMofNotUpdateWhenMofTheSameAsRepo() {
        var suspensionEvent = new SuspensionEvent(NHS_NUMBER, REPO_ODS_CODE, NEMS_MESSAGE_ID, LAST_UPDATED_DATE);
        var beforeUpdateResponse = new PdsAdaptorSuspensionStatusResponse(NHS_NUMBER, true, null,
                REPO_ODS_CODE, RECORD_E_TAG, false);

        when(toggleConfig.isCanUpdateManagingOrganisationToRepo()).thenReturn(true);
        mofService.processMofUpdate(STRING_SUSPENSION_MESSAGE, suspensionEvent, beforeUpdateResponse);

        verify(messagePublisherBroker).mofNotUpdatedMessage(NEMS_MESSAGE_ID, true);
        verify(messagePublisherBroker).activeSuspensionMessage(suspensionEvent);
        verifyNoInteractions(pdsService);
    }

    @Test
    void shouldSendMofNotUpdateWhenMofTheSameAsRepoAndCanUpdateMofToRepoIsFalse() {
        var suspensionEvent = new SuspensionEvent(NHS_NUMBER, REPO_ODS_CODE, NEMS_MESSAGE_ID, LAST_UPDATED_DATE);
        var beforeUpdateResponse = new PdsAdaptorSuspensionStatusResponse(NHS_NUMBER, true, null,
                REPO_ODS_CODE, RECORD_E_TAG, false);

        when(toggleConfig.isCanUpdateManagingOrganisationToRepo()).thenReturn(false);
        mofService.processMofUpdate(STRING_SUSPENSION_MESSAGE, suspensionEvent, beforeUpdateResponse);

        verify(messagePublisherBroker).mofNotUpdatedMessage(NEMS_MESSAGE_ID, false);
        verify(messagePublisherBroker).activeSuspensionMessage(suspensionEvent);
        verifyNoInteractions(pdsService);
    }
}