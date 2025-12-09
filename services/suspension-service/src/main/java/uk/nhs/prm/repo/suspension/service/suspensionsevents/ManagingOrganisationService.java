package uk.nhs.prm.repo.suspension.service.suspensionsevents;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.nhs.prm.repo.suspension.service.config.ToggleConfig;
import uk.nhs.prm.repo.suspension.service.model.PdsAdaptorSuspensionStatusResponse;
import uk.nhs.prm.repo.suspension.service.pds.InvalidPdsRequestException;
import uk.nhs.prm.repo.suspension.service.pds.PdsService;
import uk.nhs.prm.repo.suspension.service.publishers.MessagePublisherBroker;

@Slf4j
@Service
public class ManagingOrganisationService {

    private final PdsService pdsService;
    private final MessagePublisherBroker messagePublisherBroker;
    private final String repoOdsCode;
    private final ToggleConfig toggleConfig;
    private String allowedOdsCodes;


    public ManagingOrganisationService(PdsService pdsService, MessagePublisherBroker messagePublisherBroker, @Value("${repo.ods.code}") String repoOdsCode, ToggleConfig toggleConfig, @Value("${safe_listed_ods_codes}") String allowedOdsCodes) {
        this.pdsService = pdsService;
        this.messagePublisherBroker = messagePublisherBroker;
        this.repoOdsCode = repoOdsCode;
        this.toggleConfig = toggleConfig;
        this.allowedOdsCodes = allowedOdsCodes;
    }

    void processMofUpdate(String suspensionMessage, SuspensionEvent suspensionEvent, PdsAdaptorSuspensionStatusResponse patientStatus) {
        try {
            if (toggleConfig.isCanUpdateManagingOrganisationToRepo() && isSafeToProcess(suspensionEvent)) {
                transferToRepository(patientStatus, suspensionEvent);
            } else {
                updateMofToPreviousGp(patientStatus, suspensionEvent);
                log.info("Suspension event found from non-safe listed ODS code. Ignoring as this is not intended for ORC.");
            }
        } catch (InvalidPdsRequestException invalidPdsRequestException) {
            messagePublisherBroker.invalidMessage(suspensionMessage, suspensionEvent.getNemsMessageId());
            throw invalidPdsRequestException;
        }
    }

    private boolean isSafeToProcess(SuspensionEvent suspensionEvent) {
        log.info("Repo process only safe listed ODS code toggle is : " + toggleConfig.isRepoProcessOnlySafeListedOdsCodes());
        if (toggleConfig.isRepoProcessOnlySafeListedOdsCodes()){
            log.info("Allowed ODS codes are: " + allowedOdsCodes + " and the ODS code for the patient is: " + suspensionEvent.getPreviousOdsCode());
            return allowedOdsCodes != null && allowedOdsCodes.contains(suspensionEvent.getPreviousOdsCode());
        }
        return true;
    }

    private void updateMofToPreviousGp(PdsAdaptorSuspensionStatusResponse patientStatus, SuspensionEvent suspensionEvent) {
        if (mofIsCurrentlySetAsIntended(patientStatus.getManagingOrganisation(), suspensionEvent.previousOdsCode())) {
            log.info("Managing Organisation field is already set to previous GP");
            messagePublisherBroker.mofNotUpdatedMessage(suspensionEvent.nemsMessageId(), false);
            messagePublisherBroker.activeSuspensionMessage(suspensionEvent);
        }
        else {
            var updateMofResponse = pdsService.updateMof(patientStatus.getNhsNumber(), suspensionEvent.previousOdsCode(), patientStatus.getRecordETag());
            log.info("Managing Organisation field Updated to " + updateMofResponse.getManagingOrganisation());
            var isSuperseded = nhsNumberIsSuperseded(suspensionEvent.nhsNumber(), patientStatus.getNhsNumber());
            messagePublisherBroker.mofUpdatedMessage(suspensionEvent.nemsMessageId(), suspensionEvent.previousOdsCode(), isSuperseded);
            messagePublisherBroker.activeSuspensionMessage(suspensionEvent);
        }
    }

    private void transferToRepository(PdsAdaptorSuspensionStatusResponse pdsResponse, SuspensionEvent suspensionEvent) {
        if (mofIsCurrentlySetAsIntended(pdsResponse.getManagingOrganisation(), repoOdsCode)) {
            log.error("Managing Organisation field is already set to Repo ODS code");
            messagePublisherBroker.mofNotUpdatedMessage(suspensionEvent.nemsMessageId(), true);
            messagePublisherBroker.activeSuspensionMessage(suspensionEvent);
        }
        else {
            var updateResponse = pdsService.updateMof(pdsResponse.getNhsNumber(), repoOdsCode, pdsResponse.getRecordETag());
            log.info("Managing Organisation field Updated to REPO ODS Code " + repoOdsCode);
            messagePublisherBroker.repoIncomingMessage(updateResponse, suspensionEvent);
        }

    }

    private boolean mofIsCurrentlySetAsIntended(String currentManagingOrganisation, String intendedManagingOrganisation) {
        return currentManagingOrganisation != null && currentManagingOrganisation.equals(intendedManagingOrganisation);
    }

    private boolean nhsNumberIsSuperseded(String nemsEventNhsNumber, String pdsNhsNumber) {
        return !nemsEventNhsNumber.equals(pdsNhsNumber);
    }
}