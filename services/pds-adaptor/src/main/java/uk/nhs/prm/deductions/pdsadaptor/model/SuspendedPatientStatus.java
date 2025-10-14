package uk.nhs.prm.deductions.pdsadaptor.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SuspendedPatientStatus {

    private String nhsNumber;
    private Boolean isSuspended;
    private String currentOdsCode;
    private String managingOrganisation;
    private String recordETag;
    public Boolean isDeceased;

    public static SuspendedPatientStatus suspendedPatientStatus(String nhsNumber, String managingOrganisation, String recordETag) {
        return SuspendedPatientStatus.builder()
                .nhsNumber(nhsNumber)
                .isSuspended(true)
                .managingOrganisation(managingOrganisation)
                .recordETag(recordETag)
                .isDeceased(false)
                .build();
    }

    public static SuspendedPatientStatus nonSuspendedPatientStatus(String nhsNumber, String currentOdsCode, String managingOrganisation, String recordETag) {
        return SuspendedPatientStatus.builder()
                .nhsNumber(nhsNumber)
                .isSuspended(false)
                .currentOdsCode(currentOdsCode)
                .managingOrganisation(managingOrganisation)
                .managingOrganisation(managingOrganisation)
                .recordETag(recordETag)
                .isDeceased(false)
                .build();
    }

    public static SuspendedPatientStatus deceasedPatientStatus(String nhsNumber, String recordETag) {
        return SuspendedPatientStatus.builder()
                .nhsNumber(nhsNumber)
                .isSuspended(null)
                .recordETag(recordETag)
                .isDeceased(true)
                .build();
    }
}
