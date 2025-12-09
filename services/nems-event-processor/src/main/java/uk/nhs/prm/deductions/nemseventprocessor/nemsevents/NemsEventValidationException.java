package uk.nhs.prm.deductions.nemseventprocessor.nemsevents;

public class NemsEventValidationException extends RuntimeException {
    public NemsEventValidationException(String message) {
        super(NemsEventValidationException.class.getSimpleName() + ": " + message);
    }
}
