package uk.nhs.prm.repo.suspension.service.pds;

public class InvalidPdsRequestException extends RuntimeException {
    public InvalidPdsRequestException(String message, Throwable ex) {
        super(message, ex);
    }
}
