package uk.nhs.prm.repo.re_registration.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import uk.nhs.prm.repo.re_registration.data.ActiveSuspensionsDb;
import uk.nhs.prm.repo.re_registration.model.ActiveSuspensionsMessage;
import uk.nhs.prm.repo.re_registration.model.ReRegistrationEvent;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.nhs.prm.repo.re_registration.logging.TestLogAppender.addTestLogAppender;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActiveSuspensionsServiceTest {

    @Mock
    private ActiveSuspensionsDb activeSuspensionsDb;

    @InjectMocks
    private ActiveSuspensionsService activeSuspensionsService;

    String nhsNumber = "1234567890";
    ReRegistrationEvent reRegistrationEvent = getReRegistrationEvent("new-ods-code");
    ActiveSuspensionsMessage activeSuspensionsMessage = getActiveSuspensionsMessage("previous-ods-code", "2022-10-21T11:18:24+00:00");

    @Test
    void shouldReturnNullWhenThereIsNoActiveSuspensionRecordFoundInDb(){
        when(activeSuspensionsDb.getByNhsNumber(any())).thenReturn(null);
        activeSuspensionsService.checkActiveSuspension(reRegistrationEvent);
        assertNull(activeSuspensionsDb.getByNhsNumber(nhsNumber));
    }

    @Test
    public void shouldInvokeCallToDbWhenRequestedToGetByNhsNumber(){
        activeSuspensionsService.checkActiveSuspension(reRegistrationEvent);
        verify(activeSuspensionsDb).getByNhsNumber(activeSuspensionsMessage.getNhsNumber());
    }

    @Test
    public void shouldDeleteRecordWhenActiveSuspensionsRecordFoundByNhsNumberInDb(){
        activeSuspensionsService.deleteRecord(activeSuspensionsMessage, reRegistrationEvent);
        verify(activeSuspensionsDb).deleteByNhsNumber(activeSuspensionsMessage.getNhsNumber());
    }

    @Test
    public void shouldThrowExceptionWhenUnableToDeleteRecordInActiveSuspensionsDb(){
        doThrow(DynamoDbException.class).when(activeSuspensionsDb).deleteByNhsNumber(nhsNumber);

        Assertions.assertThrows(DynamoDbException.class, () ->
                activeSuspensionsService.deleteRecord(activeSuspensionsMessage, reRegistrationEvent));
    }

    @Test
    public void shouldNotLogWhenAReregistrationEventIsAnomalyAndReceivedForAPatientSuspendedFromSameGpWithinLastThreeDays(){
        var logAppender = addTestLogAppender();
        ActiveSuspensionsMessage suspensionMessage24HoursAgo = getActiveSuspensionsMessage("same-ods-code", "2022-10-20T11:18:24+00:00");

        activeSuspensionsService.deleteRecord(suspensionMessage24HoursAgo,getReRegistrationEvent("same-ods-code"));
        var log = logAppender.findLoggedEvent("Patient has been re-registered at a different GP practice, or at the same GP practice more than 3 days later");
        assertThat(log).isNull();
    }

    @Test
    public void shouldLogWhenReregistrationEventIsNotAnAnomalyAsItHasBeenMoreThanThreeDays(){
        var logAppender = addTestLogAppender();
        ActiveSuspensionsMessage suspensionMessage24HoursAgo = getActiveSuspensionsMessage("same-ods-code", "2022-10-17T11:18:24+00:00");

        activeSuspensionsService.deleteRecord(suspensionMessage24HoursAgo,getReRegistrationEvent("same-ods-code"));
        var log = logAppender.findLoggedEvent("Patient has been re-registered at a different GP practice, or at the same GP practice more than 3 days later");
        assertThat(log).isNotNull();
    }

    @Test
    public void shouldLogWhenReregistrationEventIsNotAnAnomalyAsDifferentGPPracticesButSuspendedLessThanThreeDaysAgo(){
        var logAppender = addTestLogAppender();
        ActiveSuspensionsMessage suspensionMessage24HoursAgo = getActiveSuspensionsMessage("same-ods-code", "2022-10-20T11:18:24+00:00");

        activeSuspensionsService.deleteRecord(suspensionMessage24HoursAgo,getReRegistrationEvent("different-ods-code"));
        var log = logAppender.findLoggedEvent("Patient has been re-registered at a different GP practice, or at the same GP practice more than 3 days later");
        assertThat(log).isNotNull();
    }

    private ReRegistrationEvent getReRegistrationEvent(String newOdsCode) {
        return new ReRegistrationEvent(nhsNumber, newOdsCode, "nemsMessageId", "2022-10-21T11:18:24+00:00");
    }

    private ActiveSuspensionsMessage getActiveSuspensionsMessage(String odsCode, String lastUpdated) {
        return new ActiveSuspensionsMessage(nhsNumber, odsCode, lastUpdated);
    }
}