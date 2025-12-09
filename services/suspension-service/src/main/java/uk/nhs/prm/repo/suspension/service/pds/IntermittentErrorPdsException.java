package uk.nhs.prm.repo.suspension.service.pds;

public class IntermittentErrorPdsException extends RuntimeException {
    public IntermittentErrorPdsException(String message, Throwable ex) {
        super(message, ex);
    }
}