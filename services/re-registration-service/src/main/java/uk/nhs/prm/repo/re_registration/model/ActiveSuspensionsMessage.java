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
public class ActiveSuspensionsMessage {

    private String nhsNumber;
    private String previousOdsCode;
    private String nemsLastUpdatedDate;

    public ActiveSuspensionsMessage(@JsonProperty("nhsNumber") String nhsNumber,
                                    @JsonProperty("previousOdsCode") String previousOdsCode,
                                    @JsonProperty("nemsLastUpdatedDate") String nemsLastUpdatedDate) {
        this.nhsNumber = nhsNumber;
        this.previousOdsCode = previousOdsCode;
        this.nemsLastUpdatedDate = nemsLastUpdatedDate;
    }

    public String toJsonString() {
        return new GsonBuilder().create().toJson(this);
    }
}
