package uk.nhs.prm.repo.suspension.service.pds;

public class UnexpectedPdsAdaptorResponseException extends RuntimeException {
    public UnexpectedPdsAdaptorResponseException(String message) {
        super(message);
    }
}
