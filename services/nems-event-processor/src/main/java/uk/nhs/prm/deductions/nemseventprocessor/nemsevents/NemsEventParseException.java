package uk.nhs.prm.deductions.nemseventprocessor.nemsevents;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NemsEventParseException extends RuntimeException {
    public NemsEventParseException(String message) {
        super(NemsEventParseException.class.getSimpleName() + ": " + message);
        log.info("Failed to parse NEMS event message: " + message);
    }
}
