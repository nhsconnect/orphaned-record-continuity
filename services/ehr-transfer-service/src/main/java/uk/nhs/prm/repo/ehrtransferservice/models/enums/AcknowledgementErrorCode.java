package uk.nhs.prm.repo.ehrtransferservice.models.enums;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum AcknowledgementErrorCode {
    ERROR_CODE_06("06", "Patient not at surgery"),
    ERROR_CODE_09("09", "EHR Extract received without corresponding request"),
    ERROR_CODE_10("10", "Failed to successfully generate EHR Extract"),
    ERROR_CODE_12("12", "Duplicate EHR Extract received");

    public final String errorCode;
    public final String displayName;
}
