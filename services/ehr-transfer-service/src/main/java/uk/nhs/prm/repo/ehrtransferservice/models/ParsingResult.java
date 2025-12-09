package uk.nhs.prm.repo.ehrtransferservice.models;

import uk.nhs.prm.repo.ehrtransferservice.models.enums.Status;

public class ParsingResult<T> {
    private final Status status;
    private final T message;

    public ParsingResult(T message, Status status) {
        this.status = status;
        this.message = message;
    }

    public Status getStatus() {
        return status;
    }

    public T getMessage() {
        return message;
    }
}
