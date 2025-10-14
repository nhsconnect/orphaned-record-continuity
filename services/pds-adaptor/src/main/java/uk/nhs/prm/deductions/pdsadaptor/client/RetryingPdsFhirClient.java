package uk.nhs.prm.deductions.pdsadaptor.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.nhs.prm.deductions.pdsadaptor.client.exceptions.RetryableRequestException;
import uk.nhs.prm.deductions.pdsadaptor.model.UpdateManagingOrganisationRequest;
import uk.nhs.prm.deductions.pdsadaptor.model.pdsresponse.PdsFhirPatient;

import java.util.UUID;
import java.util.function.Supplier;

@Component
@Slf4j
public class RetryingPdsFhirClient {

    private final int maxUpdateTries;
    private final PdsFhirClient client;

    public RetryingPdsFhirClient(PdsFhirClient pdsFhirClient,
                                 @Value("${pds.fhir.update.number.of.tries}") int maxUpdateTries) {
        this.client = pdsFhirClient;
        this.maxUpdateTries = maxUpdateTries;
    }

    public PdsFhirPatient requestPdsRecordByNhsNumber(String nhsNumber) {
        return requestWithRetries(() -> client.requestPdsRecordByNhsNumber(nhsNumber), maxUpdateTries);
    }

    public PdsFhirPatient updateManagingOrganisation(String nhsNumber, UpdateManagingOrganisationRequest updateRequest) {
        var sharedRequestIdAcrossRetries = UUID.randomUUID();
        return requestWithRetries(() ->
                client.updateManagingOrganisation(nhsNumber, updateRequest, sharedRequestIdAcrossRetries), maxUpdateTries
        );
    }

    private PdsFhirPatient requestWithRetries(Supplier<PdsFhirPatient> requestProcess, int triesLeft) {
        try {
            return requestProcess.get();
        }
        catch (RetryableRequestException retryableException) {
            if (triesLeft > 1) {
                log.error("Retrying server request, tries remaining: " + (triesLeft - 1));
                return requestWithRetries(requestProcess, triesLeft - 1);
            }
            log.error("Giving up on server request after " + maxUpdateTries + " attempts.");
            throw retryableException;
        }
    }

}
