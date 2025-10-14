package uk.nhs.prm.deductions.pdsadaptor.client.auth;

public class PdsAdaptorMisconfigurationException extends RuntimeException {
    public PdsAdaptorMisconfigurationException(String message, Exception cause) {
        super(message, cause);
    }
}
