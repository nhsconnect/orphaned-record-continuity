package uk.nhs.prm.repo.suspension.service.model;

import com.google.gson.GsonBuilder;
import lombok.AllArgsConstructor;
import lombok.Data;
import uk.nhs.prm.repo.suspension.service.suspensionsevents.SuspensionEvent;

@AllArgsConstructor
@Data
public class ActiveSuspensionsMessage {

    private final String nhsNumber;
    private final String previousOdsCode;
    private final String nemsLastUpdatedDate;

    public ActiveSuspensionsMessage(SuspensionEvent suspensionEvent) {
        this.nhsNumber = suspensionEvent.getNhsNumber();
        this.previousOdsCode = suspensionEvent.getPreviousOdsCode();
        this.nemsLastUpdatedDate = suspensionEvent.getLastUpdated();
    }

    public String toJsonString() {
        return new GsonBuilder().disableHtmlEscaping().create().toJson(this);
    }
}
