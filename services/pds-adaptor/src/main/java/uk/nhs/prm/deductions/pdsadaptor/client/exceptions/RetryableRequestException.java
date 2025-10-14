package uk.nhs.prm.deductions.pdsadaptor.client.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;

@Slf4j
@ResponseStatus(value= HttpStatus.SERVICE_UNAVAILABLE)
public class RetryableRequestException extends RuntimeException {
    public RetryableRequestException(HttpStatusCodeException e) {
        super(errorMessage(e));
        log.info(errorMessage(e));
    }

    public RetryableRequestException(ResourceAccessException e) {
        super(errorMessage(e), e);
        log.info(errorMessage(e));
    }

    private static String errorMessage(ResourceAccessException exception) {
        return String.format("PDS FHIR request had network failure: %s", exception.getMessage());
    }

    private static String errorMessage(HttpStatusCodeException e) {
        return String.format("PDS FHIR request failed status code: %s. reason %s", e.getStatusCode().value(), e.getMessage());
    }
}
