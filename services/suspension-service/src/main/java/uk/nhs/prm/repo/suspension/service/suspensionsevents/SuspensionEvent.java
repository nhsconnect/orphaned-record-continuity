package uk.nhs.prm.repo.suspension.service.suspensionsevents;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SuspensionEvent {
    private String nhsNumber;
    private String previousOdsCode;
    private String nemsMessageId;
    private String lastUpdated;

    public String nhsNumber() {
        return nhsNumber;
    }
    public String previousOdsCode() {
        return previousOdsCode;
    }
    public String nemsMessageId() {
        return nemsMessageId;
    }
    public String lastUpdated() {
        return lastUpdated;
    }
}
