package uk.nhs.prm.repo.ehrtransferservice.models.ack;

// Used to model multiple NHS HL7 specific types - e.g.:
// - https://data.developer.nhs.uk/dms/mim/6.3.01/Vocabulary/DetectedIssueQualifier.htm
// - https://data.developer.nhs.uk/dms/mim/6.3.01/Vocabulary/AcknowledgementDetailType.htm
public enum FailureLevel {
    INFO("IF"), WARNING("WG"), ERROR("ER"), UNKNOWN("");

    private final String twoCharCode;

    FailureLevel(String twoCharCode) {
        this.twoCharCode = twoCharCode;
    }

    public static FailureLevel parse(String twoCharCode) {
        for (var level : FailureLevel.values()) {
            if (level.twoCharCode.equals(twoCharCode)) {
                return level;
            }
        }
        return FailureLevel.UNKNOWN;
    }
}
