package uk.nhs.prm.deductions.pdsadaptor.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.nhs.prm.deductions.pdsadaptor.client.exceptions.PdsFhirPatchSpecifiesNoChangesException;
import uk.nhs.prm.deductions.pdsadaptor.model.UpdateManagingOrganisationRequest;
import uk.nhs.prm.deductions.pdsadaptor.model.pdspatchrequest.PdsPatch;
import uk.nhs.prm.deductions.pdsadaptor.model.pdspatchrequest.PdsPatchIdentifier;
import uk.nhs.prm.deductions.pdsadaptor.model.pdspatchrequest.PdsPatchRequest;
import uk.nhs.prm.deductions.pdsadaptor.model.pdspatchrequest.PdsPatchValue;
import uk.nhs.prm.deductions.pdsadaptor.model.pdsresponse.PdsFhirPatient;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.function.Supplier;

import static java.util.Collections.singletonList;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Slf4j
@Component
public class PdsFhirClient {
    private final AuthenticatingHttpClient httpClient;
    private final PdsFhirPatchRejectionInterpreter patchRejectionInterpreter;
    private final PdsFhirExceptionHandler exceptionHandler;
    private final String pdsFhirEndpoint;

    public PdsFhirClient(AuthenticatingHttpClient httpClient,
                         PdsFhirPatchRejectionInterpreter patchRejectionInterpreter,
                         PdsFhirExceptionHandler exceptionHandler,
                         @Value("${pdsFhirEndpoint}") String pdsFhirEndpoint) {

        this.httpClient = httpClient;
        this.patchRejectionInterpreter = patchRejectionInterpreter;
        this.exceptionHandler = exceptionHandler;
        this.pdsFhirEndpoint = pdsFhirEndpoint;
    }

    public PdsFhirPatient requestPdsRecordByNhsNumber(String nhsNumber) {
        log.info("Making GET request for pds record from pds fhir");
        return timeRequest("retrieval", () -> {
            try {
                var response = httpClient.get(patientUrl(nhsNumber), createRequestHeaders(UUID.randomUUID(), APPLICATION_JSON.toString()), PdsFhirPatient.class);
                log.info("Successfully requested pds record");
                return addEtagToResponseObject(response);
            }
            catch (RuntimeException exception) {
                throw exceptionHandler.handleCommonExceptions("requesting", exception);
            }
        });
    }

    public PdsFhirPatient updateManagingOrganisation(String nhsNumber, UpdateManagingOrganisationRequest updateRequest, UUID requestId) {
        log.info("Making PATCH request to update managing organisation via pds fhir");

        var patchRequest = createPatchRequest(updateRequest.getPreviousGp());
        var requestHeaders = createUpdateHeaders(updateRequest.getRecordETag(), requestId);

        return timeRequest("update", () -> {
            try {
                var response = httpClient.patch(patientUrl(nhsNumber), requestHeaders, patchRequest, PdsFhirPatient.class);
                log.info("Successfully updated managing organisation on pds record.");
                return addEtagToResponseObject(response);
            }
            catch (RuntimeException exception) {
                if (patchRejectionInterpreter.isRejectionDueToNotMakingChanges(exception)) {
                    log.error("Received 4xx HTTP Error from PDS FHIR when updating PDS Record");
                    throw new PdsFhirPatchSpecifiesNoChangesException();
                }
                throw exceptionHandler.handleCommonExceptions("updating", exception);
            }
        });
    }

    private HttpHeaders createUpdateHeaders(String recordETag, UUID requestId) {
        HttpHeaders headers = createRequestHeaders(requestId, "application/json-patch+json");
        headers.setIfMatch(recordETag);
        return headers;
    }

    private HttpHeaders createRequestHeaders(UUID requestId, String contentType) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Request-ID", requestId.toString());
        headers.set(HttpHeaders.CONTENT_TYPE, contentType);
        return headers;
    }

    private PdsPatchRequest createPatchRequest(String managingOrganisation) {
        var identifier = new PdsPatchIdentifier("https://fhir.nhs.uk/Id/ods-organization-code", managingOrganisation);
        var patchValue = new PdsPatchValue("Organization", identifier);
        var patch = new PdsPatch("add", "/managingOrganization", patchValue);
        return new PdsPatchRequest(singletonList(patch));
    }

    private PdsFhirPatient addEtagToResponseObject(ResponseEntity<PdsFhirPatient> response) {
        PdsFhirPatient pdsResponse = response.getBody();
        String eTag = response.getHeaders().getETag();
        pdsResponse.setETag(eTag);
        return pdsResponse;
    }

    private String patientUrl(String nhsNumber) {
        return pdsFhirEndpoint + "Patient/" + nhsNumber;
    }

    private PdsFhirPatient timeRequest(final String description, final Supplier<PdsFhirPatient> pdsRequestProcess) {
        var startTime = Instant.now();
        try {
            return pdsRequestProcess.get();
        }
        finally {
            log.info("PDS-FHIR " + description + " call took " + Duration.between(startTime, Instant.now()).toMillis() + "ms");
        }
    }
}
