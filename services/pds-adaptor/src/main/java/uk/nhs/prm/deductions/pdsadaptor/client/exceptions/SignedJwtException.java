package uk.nhs.prm.deductions.pdsadaptor.client.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import uk.nhs.prm.deductions.pdsadaptor.client.auth.PdsAdaptorMisconfigurationException;

@Slf4j
@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class SignedJwtException extends PdsAdaptorMisconfigurationException {
    public SignedJwtException(Exception e) {
        super("Unable to sign JWT", e);
        log.info("Unable to sign JWT", e);
    }

}
