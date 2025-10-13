package uk.nhs.prm.repo.ehrtransferservice.models.ack;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public enum AcknowledgementTypeCode {
    AA, AE, AR, UNKNOWN;

    public static AcknowledgementTypeCode parse(String typeCode) {
        try {
            return valueOf(typeCode);
        }
        catch (NullPointerException | IllegalArgumentException e) {
            log.warn("Unknown or missing Acknowledgement typeCode", e);
            return UNKNOWN;
        }
    }
}
