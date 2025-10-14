package uk.nhs.prm.deductions.nemseventprocessor.nemsevents;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NemsEventValidator {
    private static final String nhsNumberVerificationStatus = "01";
    public void validate(String nhsNumber, String nhsNumberValidationValue, String odsCode) {
        validateNhsNumber(nhsNumber, nhsNumberValidationValue);
        validatePreviousGpOdsCode(odsCode);
    }

    private void validateNhsNumber(String nhsNumber, String validationValue) {
        if (nhsNumber.length() != 10) {
            throw new NemsEventValidationException("NHS Number is not 10 digits");
        } else if (!validationValue.equalsIgnoreCase(nhsNumberVerificationStatus)) {
            throw new NemsEventValidationException("NHS Number verification code does not equal " + nhsNumberVerificationStatus);
        }
    }

    private void validatePreviousGpOdsCode(String odsCode) {
        if (odsCode.length() > 10) {
            throw new NemsEventValidationException("Previous GP ODS Code is more than 10 characters");
        }
    }
}
