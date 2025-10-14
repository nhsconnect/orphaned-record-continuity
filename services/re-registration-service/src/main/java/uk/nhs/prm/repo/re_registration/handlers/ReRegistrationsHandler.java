package uk.nhs.prm.repo.re_registration.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import uk.nhs.prm.repo.re_registration.audit.AuditMessages;
import uk.nhs.prm.repo.re_registration.config.ToggleConfig;
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
import uk.nhs.prm.repo.re_registration.service.ActiveSuspensionsService;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReRegistrationsHandler {

    private final ReRegistrationParser parser;
    private final PdsAdaptorService pdsAdaptorService;
    private final ToggleConfig toggleConfig;
    private final ReRegistrationAuditPublisher auditPublisher;
    private final EhrRepoService ehrRepoService;
    private final ActiveSuspensionsService activeSuspensionsService;

    public void process(String payload) {
        log.info("RECEIVED: Re-registrations Event Message");
        var reRegistrationEvent = parser.parse(payload);
        var activeSuspensionsRecord = activeSuspensionsService.checkActiveSuspension(reRegistrationEvent);

        if (activeSuspensionsRecord != null) {
            processActiveReregistration(reRegistrationEvent, activeSuspensionsRecord);
        } else {
            auditUnknownReRegistrations(reRegistrationEvent);
        }
    }

    private void processActiveReregistration(ReRegistrationEvent reRegistrationEvent, ActiveSuspensionsMessage activeSuspensionsRecord) {
        if (toggleToSendDeleteEhr()) {
            if (!isSuspendedOnPds(reRegistrationEvent)) {
                handleReRegistrationsForPreviousSuspensions(reRegistrationEvent, activeSuspensionsRecord);
            }

        } else {
            activeSuspensionsService.deleteRecord(activeSuspensionsRecord, reRegistrationEvent);
            sendAuditMessage(reRegistrationEvent, AuditMessages.NOT_PROCESSING_REREGISTRATIONS.status());
        }
    }

    private void auditUnknownReRegistrations(ReRegistrationEvent reRegistrationEvent) {
        log.info("Not a re-registration for MOF updated patient.");
        sendAuditMessage(reRegistrationEvent, AuditMessages.UNKNOWN_REREGISTRATIONS.status());
    }

    private boolean toggleToSendDeleteEhr() {
        var canSendDeleteEhrRequest = toggleConfig.canSendDeleteEhrRequest();
        log.info("Toggle canSendDeleteEhrRequest : {} ", canSendDeleteEhrRequest);
        return canSendDeleteEhrRequest;
    }

    private void handleReRegistrationsForPreviousSuspensions(ReRegistrationEvent reRegistrationEvent, ActiveSuspensionsMessage activeSuspensionsRecord) {
        log.info("Patient is not suspended, going ahead invoking ehr repo to delete records");
        deleteEhr(reRegistrationEvent);
        activeSuspensionsService.deleteRecord(activeSuspensionsRecord, reRegistrationEvent);
    }

    private boolean isSuspendedOnPds(ReRegistrationEvent reRegistrationEvent) {
        try {
            var pdsAdaptorResponse = pdsAdaptorService.getPatientPdsStatus(reRegistrationEvent);
            if (pdsAdaptorResponse.isSuspended()) {
                log.info("Patient is suspended, no need to invoke ehr repo to delete records");
                sendAuditMessage(reRegistrationEvent, AuditMessages.STILL_SUSPENDED.status());
                return true;
            }
        } catch (HttpStatusCodeException e) {
            handlePdsErrorResponse(reRegistrationEvent, e);
            return true;
        }
        return false;
    }

    private void handlePdsErrorResponse(ReRegistrationEvent reRegistrationEvent, HttpStatusCodeException e) {
        if (e.getStatusCode().is4xxClientError()) {
            log.info("Encountered client error with status code : {}", e.getStatusCode());
            sendAuditMessage(reRegistrationEvent, AuditMessages.PDS_ERROR.status());
        } else if (e.getStatusCode().is5xxServerError()) {
            log.info("Caught retryable exception: " + e.getMessage());
            log.info("Encountered server error with status code : {}", e.getStatusCode());
            throw new IntermittentErrorPdsException("Encountered error when calling pds get patient endpoint", e);
        }
    }

    private void deleteEhr(ReRegistrationEvent reRegistrationEvent) {
        try {
            var ehrDeleteResponse = ehrRepoService.deletePatientEhr(reRegistrationEvent);
            var deleteAuditMessage = new DeleteAuditMessage(reRegistrationEvent.getNemsMessageId(), ehrDeleteResponse.getConversationIds());
            auditPublisher.sendMessage(deleteAuditMessage);
        } catch (HttpStatusCodeException e) {
            handleEhrRepoErrorResponse(reRegistrationEvent, e);
        } catch (JsonProcessingException e) {
            log.info("error during the mapping of delete response");
        }
    }

    private void handleEhrRepoErrorResponse(ReRegistrationEvent reRegistrationEvent, HttpStatusCodeException e) {
        if (e.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
            log.info("Encountered client error with status code : {}", e.getStatusCode());
            auditPublisher.sendMessage(new AuditMessage(reRegistrationEvent.getNemsMessageId(), AuditMessages.EHR_NOT_IN_REPO.status()));
        } else if (e.getStatusCode().equals(HttpStatus.BAD_REQUEST)) {
            log.info("Encountered client error with status code : {}", e.getStatusCode());
            auditPublisher.sendMessage(new AuditMessage(reRegistrationEvent.getNemsMessageId(), AuditMessages.FAILURE_TO_DELETE_EHR.status()));
        } else if (e.getStatusCode().is5xxServerError()) {
            log.info("Encountered server error with status code : {}", e.getStatusCode());
            throw new EhrRepoServerException("Encountered error when calling ehr-repo DELETE patient endpoint", e);
        }
    }

    private void sendAuditMessage(ReRegistrationEvent reRegistrationEvent, String status) {
        auditPublisher.sendMessage(new AuditMessage(reRegistrationEvent.getNemsMessageId(), status));
    }
}