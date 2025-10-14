package uk.nhs.prm.deductions.pdsadaptor.client.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Slf4j
@ResponseStatus(value= HttpStatus.SERVICE_UNAVAILABLE)
public class TooManyRequestsException extends RuntimeException {
    public TooManyRequestsException() {
        super("Rate limit exceeded for PDS FHIR - too many requests");
        log.info("Rate limit exceeded for PDS FHIR - too many requests");
    }
}