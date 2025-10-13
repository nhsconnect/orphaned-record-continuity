package uk.nhs.prm.repo.ehrtransferservice.exceptions;

public class HttpException extends Exception {
    public HttpException() {
        super();
    }

    public HttpException(String message) {
        super(message);
    }
}
