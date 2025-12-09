package uk.nhs.prm.repo.suspension.service.model;

import com.google.gson.GsonBuilder;
import lombok.AllArgsConstructor;
import lombok.Data;
import uk.nhs.prm.repo.suspension.service.suspensionsevents.SuspensionEvent;

@Data
@AllArgsConstructor
public class RepoIncomingEvent {
    private String nhsNumber;

    private String destinationGp;

    private String sourceGp;

    private String nemsMessageId;

    private String conversationId;

    private String nemsEventLastUpdated;

    public RepoIncomingEvent(PdsAdaptorSuspensionStatusResponse pdsAdaptorSuspensionStatusResponse, SuspensionEvent suspensionEvent, String conversationId) {
        this.nhsNumber = pdsAdaptorSuspensionStatusResponse.getNhsNumber();
        this.destinationGp = pdsAdaptorSuspensionStatusResponse.getManagingOrganisation();
        this.sourceGp = suspensionEvent.getPreviousOdsCode();
        this.nemsMessageId = suspensionEvent.getNemsMessageId();
        this.conversationId = conversationId;
        this.nemsEventLastUpdated = suspensionEvent.lastUpdated();
    }

    public String toJsonString() {
        return new GsonBuilder().disableHtmlEscaping().create()
                .toJson(this);
    }
}
