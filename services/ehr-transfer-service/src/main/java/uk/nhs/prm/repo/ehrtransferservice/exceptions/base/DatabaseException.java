package uk.nhs.prm.repo.ehrtransferservice.exceptions.base;

public abstract class DatabaseException extends RuntimeException {
    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
