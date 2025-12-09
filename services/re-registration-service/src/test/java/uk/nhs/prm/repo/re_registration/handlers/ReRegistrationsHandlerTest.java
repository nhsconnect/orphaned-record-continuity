package uk.nhs.prm.repo.re_registration.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import uk.nhs.prm.repo.re_registration.config.ToggleConfig;
import uk.nhs.prm.repo.re_registration.ehr_repo.EhrDeleteResponseContent;
import uk.nhs.prm.repo.re_registration.ehr_repo.EhrRepoServerException;
import uk.nhs.prm.repo.re_registration.ehr_repo.EhrRepoService;
import uk.nhs.prm.repo.re_registration.message_publishers.ReRegistrationAuditPublisher;
import uk.nhs.prm.repo.re_registration.model.ActiveSuspensionsMessage;
import uk.nhs.prm.repo.re_registration.model.AuditMessage;
import uk.nhs.prm.repo.re_registration.model.DeleteAuditMessage;
import uk.nhs.prm.repo.re_registration.model.ReRegistrationEvent;
import uk.nhs.prm.repo.re_registration.parser.ReRegistrationParser;
import uk.nhs.prm.repo.re_registration.pds.IntermittentErrorPdsException;
import uk.nhs.prm.repo.re_registration.pds.PdsAdaptorService;
import uk.nhs.prm.repo.re_registration.pds.model.PdsAdaptorSuspensionStatusResponse;
import uk.nhs.prm.repo.re_registration.service.ActiveSuspensionsService;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReRegistrationsHandlerTest {

    @Mock
    private PdsAdaptorService pdsAdaptorService;

    @Mock
    private ReRegistrationParser parser;

    @Mock
    private ToggleConfig toggleConfig;

    @Mock
    private ReRegistrationAuditPublisher auditPublisher;

    @Mock
    private EhrRepoService ehrRepoService;

    @Mock
    ActiveSuspensionsService activeSuspensionsService;

    @InjectMocks
    private ReRegistrationsHandler reRegistrationsHandler;

    private final ReRegistrationEvent reRegistrationEvent = createReRegistrationEvent();

    @BeforeEach
    void setUp() {
        when(parser.parse(any())).thenReturn(reRegistrationEvent);
    }


    @Test
    void shouldCallPdsAdaptorServiceToGetPatientsPdsStatusWhenHandleMessageIsInvoked() {
        when(toggleConfig.canSendDeleteEhrRequest()).thenReturn(true);
        when(pdsAdaptorService.getPatientPdsStatus(any())).thenReturn(getPdsResponseStringWithSuspendedStatus(true));
        when(activeSuspensionsService.checkActiveSuspension(any())).thenReturn(getActiveSuspensionsMessage());
        reRegistrationsHandler.process(reRegistrationEvent.toJsonString());
        verify(pdsAdaptorService, times(1)).getPatientPdsStatus(reRegistrationEvent);
    }

    @Test
    void shouldNotCallPdsAndSendMessageToAuditTopicWithCanSendDeleteEhrRequestIsFalse() {
        when(toggleConfig.canSendDeleteEhrRequest()).thenReturn(false);
        when(activeSuspensionsService.checkActiveSuspension(any())).thenReturn(getActiveSuspensionsMessage());
        reRegistrationsHandler.process(reRegistrationEvent.toJsonString());
        verifyNoInteractions(pdsAdaptorService);
        var auditMessage = new AuditMessage("nemsMessageId", "NO_ACTION:RE_REGISTRATION_EVENT_RECEIVED");
        verify(auditPublisher).sendMessage(auditMessage);
    }

    @Test
    void shouldPublishStatusMessageOnAuditTopicWhenPDSAdaptorReturns4xxError() {
        when(toggleConfig.canSendDeleteEhrRequest()).thenReturn(true);
        when(pdsAdaptorService.getPatientPdsStatus(any())).thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));
        when(activeSuspensionsService.checkActiveSuspension(any())).thenReturn(getActiveSuspensionsMessage());
        reRegistrationsHandler.process(reRegistrationEvent.toJsonString());
        verify(auditPublisher).sendMessage(new AuditMessage("nemsMessageId", "NO_ACTION:RE_REGISTRATION_FAILED_PDS_ERROR"));
    }

    @Test
    void shouldThrowAnIntermittentErrorPdsExceptionWhenPDSAdaptorReturns5xxError() {
        when(toggleConfig.canSendDeleteEhrRequest()).thenReturn(true);
        when(activeSuspensionsService.checkActiveSuspension(any())).thenReturn(getActiveSuspensionsMessage());
        when(pdsAdaptorService.getPatientPdsStatus(any())).thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));
        assertThrows(IntermittentErrorPdsException.class, () -> reRegistrationsHandler.process(reRegistrationEvent.toJsonString()));
    }

    @Test
    void shouldPublishToQueueWhenPatientIsSuspendedWithNoAction() {
        when(toggleConfig.canSendDeleteEhrRequest()).thenReturn(true);
        when(pdsAdaptorService.getPatientPdsStatus(any())).thenReturn(getPdsResponseStringWithSuspendedStatus(true));
        when(activeSuspensionsService.checkActiveSuspension(any())).thenReturn(getActiveSuspensionsMessage());
        reRegistrationsHandler.process(reRegistrationEvent.toJsonString());
        verify(auditPublisher).sendMessage(new AuditMessage("nemsMessageId", "NO_ACTION:RE_REGISTRATION_FAILED_STILL_SUSPENDED"));
    }

    @Test
    void shouldPublishToQueueWhenPatientIsNotSuspendedWithAction() throws JsonProcessingException {
        when(toggleConfig.canSendDeleteEhrRequest()).thenReturn(true);
        when(activeSuspensionsService.checkActiveSuspension(any())).thenReturn(getActiveSuspensionsMessage());
        when(pdsAdaptorService.getPatientPdsStatus(any())).thenReturn(getPdsResponseStringWithSuspendedStatus(false));
        when(ehrRepoService.deletePatientEhr(any())).thenReturn(createSuccessfulEhrDeleteResponse());
        reRegistrationsHandler.process(reRegistrationEvent.toJsonString());
        verify(ehrRepoService).deletePatientEhr(reRegistrationEvent);
        verify(auditPublisher).sendMessage(new DeleteAuditMessage("nemsMessageId", Arrays.asList("2431d4ff-f760-4ab9-8cd8-a3fc47846762", "c184cc19-86e9-4a95-b5b5-2f156900bb3c")));
    }


    @Test
    void shouldPublishStatusMessageOnAuditTopicWhenEhrResponseReturns404Error() throws JsonProcessingException {
        when(toggleConfig.canSendDeleteEhrRequest()).thenReturn(true);
        when(activeSuspensionsService.checkActiveSuspension(any())).thenReturn(getActiveSuspensionsMessage());
        when(pdsAdaptorService.getPatientPdsStatus(any())).thenReturn(getPdsResponseStringWithSuspendedStatus(false));
        when(ehrRepoService.deletePatientEhr(any())).thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND) {});
        reRegistrationsHandler.process(reRegistrationEvent.toJsonString());
        verify(auditPublisher, times(1)).sendMessage(new AuditMessage("nemsMessageId", "NO_ACTION:RE_REGISTRATION_EHR_NOT_IN_REPO"));
    }

    @Test
    void shouldPublishStatusMessageOnAuditTopicWhenEhrResponseReturns400Error() throws JsonProcessingException {
        when(toggleConfig.canSendDeleteEhrRequest()).thenReturn(true);
        when(pdsAdaptorService.getPatientPdsStatus(any())).thenReturn(getPdsResponseStringWithSuspendedStatus(false));
        when(activeSuspensionsService.checkActiveSuspension(any())).thenReturn(getActiveSuspensionsMessage());
        when(ehrRepoService.deletePatientEhr(any())).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST) {});
        when(activeSuspensionsService.checkActiveSuspension(any())).thenReturn(getActiveSuspensionsMessage());
        reRegistrationsHandler.process(reRegistrationEvent.toJsonString());
        verify(auditPublisher, times(1)).sendMessage(new AuditMessage("nemsMessageId", "NO_ACTION:RE_REGISTRATION_EHR_FAILED_TO_DELETE"));
    }

    @Test
    void shouldThrowAServerErrorExceptionWhenEhrResponseReturns5xxError() throws JsonProcessingException {
        when(toggleConfig.canSendDeleteEhrRequest()).thenReturn(true);
        when(pdsAdaptorService.getPatientPdsStatus(any())).thenReturn(getPdsResponseStringWithSuspendedStatus(false));
        when(ehrRepoService.deletePatientEhr(any())).thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR) {});
        when(activeSuspensionsService.checkActiveSuspension(any())).thenReturn(getActiveSuspensionsMessage());
        assertThrows(EhrRepoServerException.class, () -> reRegistrationsHandler.process(reRegistrationEvent.toJsonString()));
    }

    @Test
    void shouldCallHandleActiveSuspensionWhenDeleteEhrToggleIsFalseAndSuspensionActive() {
        when(toggleConfig.canSendDeleteEhrRequest()).thenReturn(false);
        var activeSuspensionsMessage = getActiveSuspensionsMessage();
        when(activeSuspensionsService.checkActiveSuspension(any())).thenReturn(activeSuspensionsMessage);
        reRegistrationsHandler.process(reRegistrationEvent.toJsonString());
        verifyNoInteractions(pdsAdaptorService);
        var auditMessage = new AuditMessage("nemsMessageId", "NO_ACTION:RE_REGISTRATION_EVENT_RECEIVED");
        verify(auditPublisher).sendMessage(auditMessage);
        verify(activeSuspensionsService).deleteRecord(activeSuspensionsMessage, reRegistrationEvent);
    }

    @Test
    void shouldPublishUnknownReRegistrationMessageToAuditTopicWhenThereIsNoActiveSuspensionRecordFoundInDb(){
        when(activeSuspensionsService.checkActiveSuspension(any())).thenReturn(null);
        reRegistrationsHandler.process(reRegistrationEvent.toJsonString());
        verify(auditPublisher, times(1)).sendMessage(new AuditMessage("nemsMessageId", "NO_ACTION:UNKNOWN_REGISTRATION_EVENT_RECEIVED"));
    }

    @Test
    void shouldInvokeHandleActiveSuspensionsWhenReRegistrationReceivedAndActiveSuspensionsFoundInDB() throws JsonProcessingException {
        when(toggleConfig.canSendDeleteEhrRequest()).thenReturn(true);
        when(activeSuspensionsService.checkActiveSuspension(any())).thenReturn(getActiveSuspensionsMessage());
        when(pdsAdaptorService.getPatientPdsStatus(any())).thenReturn(getPdsResponseStringWithSuspendedStatus(false));
        when(ehrRepoService.deletePatientEhr(any())).thenReturn(createSuccessfulEhrDeleteResponse());
        reRegistrationsHandler.process(reRegistrationEvent.toJsonString());
        verify(activeSuspensionsService, times(1)).deleteRecord(getActiveSuspensionsMessage(), createReRegistrationEvent());
    }


    private ReRegistrationEvent createReRegistrationEvent() {
        return new ReRegistrationEvent("1234567890", "ABC123", "nemsMessageId", "2017-11-01T15:00:33+00:00");
    }

    private PdsAdaptorSuspensionStatusResponse getPdsResponseStringWithSuspendedStatus(boolean isSuspended) {
        return new PdsAdaptorSuspensionStatusResponse("0000000000", isSuspended, "currentOdsCode", "managingOrganisation", "etag", false);
    }

    private EhrDeleteResponseContent createSuccessfulEhrDeleteResponse() {
        return new EhrDeleteResponseContent("patients", "1234567890", Arrays.asList("2431d4ff-f760-4ab9-8cd8-a3fc47846762", "c184cc19-86e9-4a95-b5b5-2f156900bb3c"));
    }

    private ActiveSuspensionsMessage getActiveSuspensionsMessage(){
        return new ActiveSuspensionsMessage("1234567890", "OLD123", "2022-09-06T15:00:33+00:00");
    }
}