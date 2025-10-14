package uk.nhs.prm.deductions.nemseventprocessor.nemsevents;

import com.google.gson.Gson;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Data
public class SuspendedMessage {

    private final String nhsNumber;
    private final String lastUpdated;
    private final String previousOdsCode;
    private final String nemsMessageId;

    public SuspendedMessage (NemsEventMessage nemsEventMessage) {
        nhsNumber = nemsEventMessage.getNhsNumber();
        lastUpdated = nemsEventMessage.getLastUpdated();
        previousOdsCode = nemsEventMessage.getOdsCode();
        nemsMessageId = nemsEventMessage.getNemsMessageId();
    }

    public String getNemsMessageId() {
        return this.nemsMessageId;
    }

    public String toJsonString() {
        return new Gson().toJson(this);
    }
}
