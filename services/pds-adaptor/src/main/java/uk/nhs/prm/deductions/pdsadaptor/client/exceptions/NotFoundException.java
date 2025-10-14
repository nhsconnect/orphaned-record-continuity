package uk.nhs.prm.deductions.pdsadaptor.client.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.HttpStatusCodeException;

import static java.lang.String.format;
import static uk.nhs.prm.deductions.pdsadaptor.logging.JsonLogger.logInfoWithJson;

@Slf4j
@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class NotFoundException extends RuntimeException {
    public NotFoundException(String errorMessage, HttpStatusCodeException exception) {
        super(errorMessage, exception);
        logInfoWithJson(log, errorMessage, "error_response", exception.getResponseBodyAsString());
    }
}