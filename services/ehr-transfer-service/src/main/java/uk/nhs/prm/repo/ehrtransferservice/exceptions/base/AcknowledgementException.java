package uk.nhs.prm.repo.ehrtransferservice.exceptions.base;

public abstract class AcknowledgementException extends RuntimeException {
    public AcknowledgementException(String message, Throwable cause) {
        super(message, cause);
    }
}
