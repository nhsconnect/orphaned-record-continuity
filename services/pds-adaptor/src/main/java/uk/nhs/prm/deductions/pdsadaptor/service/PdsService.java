package uk.nhs.prm.deductions.pdsadaptor.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.nhs.prm.deductions.pdsadaptor.client.RetryingPdsFhirClient;
import uk.nhs.prm.deductions.pdsadaptor.model.SuspendedPatientStatus;
import uk.nhs.prm.deductions.pdsadaptor.model.UpdateManagingOrganisationRequest;
import uk.nhs.prm.deductions.pdsadaptor.model.pdsresponse.GeneralPractitioner;
import uk.nhs.prm.deductions.pdsadaptor.model.pdsresponse.PdsFhirPatient;

import static uk.nhs.prm.deductions.pdsadaptor.model.SuspendedPatientStatus.*;

@Service
@RequiredArgsConstructor
public class PdsService {

    private final RetryingPdsFhirClient pdsFhirClient;

    public SuspendedPatientStatus getPatientGpStatus(String nhsNumber) {
        PdsFhirPatient pdsResponse = pdsFhirClient.requestPdsRecordByNhsNumber(nhsNumber);
        return convertToPatientStatusObject(pdsResponse);
    }

    public SuspendedPatientStatus updatePatientManagingOrganisation(String nhsNumber, UpdateManagingOrganisationRequest updateRequest) {
        PdsFhirPatient pdsResponse = pdsFhirClient.updateManagingOrganisation(nhsNumber, updateRequest);
        return convertToPatientStatusObject(pdsResponse);
    }

    private SuspendedPatientStatus convertToPatientStatusObject(PdsFhirPatient pdsResponse) {
        if (hasDeceasedDateAndTime(pdsResponse)) {
            return deceasedPatientStatus(pdsResponse.getId(), pdsResponse.getETag());
        }
        if (hasGeneralPractitioner(pdsResponse)) {
            return nonSuspendedPatientStatus(pdsResponse.getId(), getOdsCode(pdsResponse), getManagingOrganisation(pdsResponse), pdsResponse.getETag());
        }
        return suspendedPatientStatus(pdsResponse.getId(), getManagingOrganisation(pdsResponse), pdsResponse.getETag());
    }

    private String getOdsCode(PdsFhirPatient pdsResponse) {
        GeneralPractitioner generalPractitioner = pdsResponse.getGeneralPractitioner().get(0);
        return generalPractitioner.getIdentifier().getValue();
    }

    private boolean hasGeneralPractitioner(PdsFhirPatient pdsResponse) {
        return pdsResponse.getGeneralPractitioner() != null && pdsResponse.getGeneralPractitioner().size() != 0;
    }

    private boolean hasDeceasedDateAndTime(PdsFhirPatient pdsResponse) {
        return pdsResponse.getDeceasedDateTime() != null;
    }

    private String getManagingOrganisation(PdsFhirPatient pdsResponse) {
       if (pdsResponse.getManagingOrganization() != null) {
           return pdsResponse.getManagingOrganization().getIdentifier().getValue();
       }
       return null;
    }
}
