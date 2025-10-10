package uk.nhs.prm.deductions.nemseventprocessor.nemsevents;

public interface NemsEventHandler {
    void processNemsEvent(String message);
}
