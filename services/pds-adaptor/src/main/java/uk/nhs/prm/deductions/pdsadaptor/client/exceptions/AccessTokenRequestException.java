package uk.nhs.prm.deductions.pdsadaptor.client.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.HttpStatusCodeException;

@Slf4j
@ResponseStatus(value = HttpStatus.SERVICE_UNAVAILABLE)
public class AccessTokenRequestException extends RuntimeException {
    public AccessTokenRequestException(HttpStatusCodeException e) {
        super("Access token request failed", e);
        log.info("Access token request failed - status code: {}, error: {}", e.getStatusCode().value(), e.getMessage());
    }

    public AccessTokenRequestException(String message, Exception e) {
        super(message, e);
    }
}
