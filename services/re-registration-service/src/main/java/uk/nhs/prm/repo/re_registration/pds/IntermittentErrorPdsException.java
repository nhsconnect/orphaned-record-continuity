package uk.nhs.prm.repo.re_registration.pds;

public class IntermittentErrorPdsException extends RuntimeException {
    public IntermittentErrorPdsException(String message, Throwable ex) {
        super(message, ex);
    }
}