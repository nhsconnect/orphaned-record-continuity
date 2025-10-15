package uk.nhs.prm.repo.suspension.service.suspensionsevents;

public class InvalidSuspensionMessageException extends RuntimeException {
    public InvalidSuspensionMessageException(String message, Throwable ex) {
        super(message, ex);
    }
}
