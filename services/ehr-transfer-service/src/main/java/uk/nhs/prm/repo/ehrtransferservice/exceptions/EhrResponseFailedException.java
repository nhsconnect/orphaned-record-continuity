package uk.nhs.prm.repo.ehrtransferservice.exceptions;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EhrResponseFailedException extends RuntimeException {
    private static final String BASE_MESSAGE = "EHR Request has ended in failure, transfer status is: %s";

    public EhrResponseFailedException(String message) {
        super(String.format(BASE_MESSAGE, message));
    }
}
