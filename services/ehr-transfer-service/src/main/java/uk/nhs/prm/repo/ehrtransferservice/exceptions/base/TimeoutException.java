package uk.nhs.prm.repo.ehrtransferservice.exceptions.base;

public abstract class TimeoutException extends RuntimeException {
    public TimeoutException(String message) {
        super(message);
    }
}
