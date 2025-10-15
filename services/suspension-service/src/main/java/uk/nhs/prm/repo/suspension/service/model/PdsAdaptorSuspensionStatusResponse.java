package uk.nhs.prm.repo.suspension.service.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PdsAdaptorSuspensionStatusResponse {
        private final String nhsNumber;
        private final Boolean isSuspended;
        private final String currentOdsCode;
        private final String managingOrganisation;
        private final String recordETag;
        private final Boolean isDeceased;

        public PdsAdaptorSuspensionStatusResponse(@JsonProperty("nhsNumber") String nhsNumber,
                                                  @JsonProperty("isSuspended") Boolean isSuspended,
                                                  @JsonProperty("currentOdsCode") String currentOdsCode,
                                                  @JsonProperty("managingOrganisation") String managingOrganisation,
                                                  @JsonProperty("recordETag") String recordETag,
                                                  @JsonProperty("isDeceased") Boolean isDeceased) {
                this.nhsNumber = nhsNumber;
                this.isSuspended = isSuspended;
                this.currentOdsCode = currentOdsCode;
                this.managingOrganisation = managingOrganisation;
                this.recordETag = recordETag;
                this.isDeceased = isDeceased;
        }
}
