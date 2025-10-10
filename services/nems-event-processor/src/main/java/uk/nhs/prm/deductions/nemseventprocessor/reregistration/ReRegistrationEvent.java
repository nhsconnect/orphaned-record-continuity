package uk.nhs.prm.deductions.nemseventprocessor.reregistration;

import com.google.gson.GsonBuilder;
import lombok.Data;
import uk.nhs.prm.deductions.nemseventprocessor.nemsevents.NemsEventMessage;

@Data
public class ReRegistrationEvent {
    private final String nhsNumber;
    private final String newlyRegisteredOdsCode;
    private final String nemsMessageId;
    private final String lastUpdated;

    public ReRegistrationEvent(String nhsNumber, String newlyRegisteredOdsCode, String nemsMessageId, String lastUpdated) {
        this.nhsNumber = nhsNumber;
        this.newlyRegisteredOdsCode = newlyRegisteredOdsCode;
        this.nemsMessageId = nemsMessageId;
        this.lastUpdated = lastUpdated;
    }

    public ReRegistrationEvent(NemsEventMessage nemsEventMessage) {
        this.nhsNumber = nemsEventMessage.getNhsNumber();
        this.newlyRegisteredOdsCode = nemsEventMessage.getOdsCode();
        this.nemsMessageId = nemsEventMessage.getNemsMessageId();
        this.lastUpdated = nemsEventMessage.getLastUpdated();
    }

    public String toJsonString() {
        return new GsonBuilder().disableHtmlEscaping().create()
                .toJson(this);
    }
}
