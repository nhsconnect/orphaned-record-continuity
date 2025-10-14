package uk.nhs.prm.repo.re_registration.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.nhs.prm.repo.re_registration.data.ActiveSuspensionsDb;
import uk.nhs.prm.repo.re_registration.model.ActiveSuspensionsMessage;
import uk.nhs.prm.repo.re_registration.model.ReRegistrationEvent;

import java.time.LocalDateTime;

@Service
@Slf4j
public class ActiveSuspensionsService {
    private final ActiveSuspensionsDb activeSuspensionsDb;

    public ActiveSuspensionsService(ActiveSuspensionsDb activeSuspensionsDb) {
        this.activeSuspensionsDb = activeSuspensionsDb;
    }

    public ActiveSuspensionsMessage checkActiveSuspension(ReRegistrationEvent reRegistrationEvent) {
        return activeSuspensionsDb.getByNhsNumber(reRegistrationEvent.getNhsNumber());
    }

    public void deleteRecord(ActiveSuspensionsMessage activeSuspensionsRecord, ReRegistrationEvent reRegistrationEvent) {
        log.info("Re-registration event received for suspended patient. From {} to {} at {}", activeSuspensionsRecord.getPreviousOdsCode(), reRegistrationEvent.getNewlyRegisteredOdsCode(), reRegistrationEvent.getLastUpdated());
        filterOutAnomalies(activeSuspensionsRecord, reRegistrationEvent);

        activeSuspensionsDb.deleteByNhsNumber(activeSuspensionsRecord.getNhsNumber());
        log.info("Successfully deleted active-suspensions record from the DB.");
    }

    private void filterOutAnomalies(ActiveSuspensionsMessage activeSuspensionsRecord, ReRegistrationEvent reRegistrationEvent) {
        // Anomaly = When we receive a re-registration event for a patient who was suspended from the same ODS code less than 3 days before
        var patientHasBeenReregisteredToSameOdsCode = activeSuspensionsRecord.getPreviousOdsCode().equals(reRegistrationEvent.getNewlyRegisteredOdsCode());
        LocalDateTime suspensionDateTime = LocalDateTime.parse(activeSuspensionsRecord.getNemsLastUpdatedDate().substring(0, 19));
        LocalDateTime reregistrationDateTime = LocalDateTime.parse(reRegistrationEvent.getLastUpdated().substring(0, 19));

        boolean isAnAnomaly = patientHasBeenReregisteredToSameOdsCode && suspensionDateTime.isAfter(reregistrationDateTime.minusDays(3));

        if (isAnAnomaly) {
            log.info("Anomaly - Patient was re-registered at the same ODS Code - {} - they were suspended from on date - {}", activeSuspensionsRecord.getPreviousOdsCode(), activeSuspensionsRecord.getNemsLastUpdatedDate());
        } else {
            log.info("Patient has been re-registered at a different GP practice, or at the same GP practice more than 3 days later");
        }
    }
}
