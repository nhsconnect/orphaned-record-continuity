package uk.nhs.prm.repo.ehrtransferservice.exceptions;

public class DuplicateMessageException extends Exception {
    public DuplicateMessageException(String message) {
        super(message);
    }
}