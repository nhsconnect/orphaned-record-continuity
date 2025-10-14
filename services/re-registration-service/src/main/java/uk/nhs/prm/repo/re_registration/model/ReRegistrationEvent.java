package uk.nhs.prm.repo.re_registration.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.GsonBuilder;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReRegistrationEvent {
    private String nhsNumber;
    private String newlyRegisteredOdsCode;
    private String nemsMessageId;
    private String lastUpdated;

    public ReRegistrationEvent(@JsonProperty("nhsNumber") String nhsNumber,
                               @JsonProperty("newlyRegisteredOdsCode") String newlyRegisteredOdsCode,
                               @JsonProperty("nemsMessageId") String nemsMessageId,
                               @JsonProperty("lastUpdated") String lastUpdated) {
        this.nhsNumber = nhsNumber;
        this.newlyRegisteredOdsCode = newlyRegisteredOdsCode;
        this.nemsMessageId = nemsMessageId;
        this.lastUpdated = lastUpdated;
    }

    public String toJsonString() {
        return new GsonBuilder().create().toJson(this);
    }
}
