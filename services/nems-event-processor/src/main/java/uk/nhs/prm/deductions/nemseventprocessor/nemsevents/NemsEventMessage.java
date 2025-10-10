package uk.nhs.prm.deductions.nemseventprocessor.nemsevents;

import com.google.gson.Gson;

public class NemsEventMessage {
    private final NemsEventType eventType;
    private final String nhsNumber;
    private final String lastUpdated;
    private final String odsCode;
    private final String nemsMessageId;

    public static NemsEventMessage suspension(String nhsNumber, String lastUpdated, String odsCode, String nemsMessageId) {
        return new NemsEventMessage(NemsEventType.SUSPENSION, nhsNumber, lastUpdated, odsCode, nemsMessageId);
    }

    public static NemsEventMessage reRegistration(String nhsNumber, String lastUpdated, String odsCode, String nemsMessageId) {
        return new NemsEventMessage(NemsEventType.REREGISTRATION, nhsNumber, lastUpdated, odsCode, nemsMessageId);
    }

    public static NemsEventMessage nonSuspension(String nemsMessageId) {
        return new NemsEventMessage(NemsEventType.NON_SUSPENSION, null, null, null, nemsMessageId);
    }

    public boolean isSuspension() {
        return this.eventType == NemsEventType.SUSPENSION;
    }

    public boolean isReRegistration() {
        return this.eventType == NemsEventType.REREGISTRATION;
    }

    private NemsEventMessage(NemsEventType eventType, String nhsNumber, String lastUpdated, String previousOdsCode, String nemsMessageId) {
        this.eventType = eventType;
        this.nhsNumber = nhsNumber;
        this.lastUpdated = lastUpdated;
        this.odsCode = previousOdsCode;
        this.nemsMessageId = nemsMessageId;
    }

    public String getNemsMessageId() {
        return this.nemsMessageId;
    }

    public String getOdsCode() {
        return this.odsCode;
    }

    public String getNhsNumber() {
        return this.nhsNumber;
    }

    public String getLastUpdated() {
        return this.lastUpdated;
    }

    public String toJsonString() {
        return new Gson().toJson(this);
    }
}
