package uk.nhs.prm.deductions.pdsadaptor.model.pdsresponse;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder(toBuilder = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PdsFhirPatient {
    private String id;
    private List<GeneralPractitioner> generalPractitioner;
    private ManagingOrganization managingOrganization;
    private String eTag;
    private String deceasedDateTime;
}
