package uk.nhs.prm.repo.ehrtransferservice.exceptions;

public class EhrDeleteRequestException extends RuntimeException {
    private static final String BASE_MESSAGE = "Unexpected response from EHR Deletion request, details: %s.";

    public EhrDeleteRequestException(String message) {
        super(String.format(BASE_MESSAGE, message));
    }
}
