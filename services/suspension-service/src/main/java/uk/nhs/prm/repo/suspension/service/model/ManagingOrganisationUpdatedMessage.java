package uk.nhs.prm.repo.suspension.service.model;

import com.google.gson.GsonBuilder;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class ManagingOrganisationUpdatedMessage {

    private final String nemsMessageId;
    private final String managingOrganisationOdsCode;
    private final String status;

    public String toJsonString() {
        return new GsonBuilder().disableHtmlEscaping().create().toJson(this);
    }
}
