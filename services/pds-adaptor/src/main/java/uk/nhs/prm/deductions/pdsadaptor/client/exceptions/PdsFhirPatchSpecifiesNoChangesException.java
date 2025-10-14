package uk.nhs.prm.deductions.pdsadaptor.client.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Slf4j
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class PdsFhirPatchSpecifiesNoChangesException extends RuntimeException {
    public PdsFhirPatchSpecifiesNoChangesException() {
        super("PDS FHIR request failed status code: 400. reason Provided patch made no changes to the resource");
        log.info("PDS FHIR request failed status code: 400. reason Provided patch made no changes to the resource");
    }

}
