package uk.nhs.prm.repo.ehrtransferservice.models.ack;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AcknowledgementTypeCodeTest {

    @Test
    void shouldParse_AA_asPositiveAcknowledgementAcceptType() {
        assertThat(AcknowledgementTypeCode.parse("AA")).isEqualTo(AcknowledgementTypeCode.AA);
    }

    @Test
    void shouldParse_AE_asNegativeAcknowledgementErrorType_ProbablyFromSendingSystem() {
        assertThat(AcknowledgementTypeCode.parse("AE")).isEqualTo(AcknowledgementTypeCode.AE);
    }

    @Test
    void shouldParse_AR_asNegativeAcknowledgementRejectType_ProbablyFromSpine() {
        assertThat(AcknowledgementTypeCode.parse("AR")).isEqualTo(AcknowledgementTypeCode.AR);
    }

    @Test
    void shouldParseMissingTypeCodeAsUnknown() {
        assertThat(AcknowledgementTypeCode.parse(null)).isEqualTo(AcknowledgementTypeCode.UNKNOWN);
    }

    @Test
    void shouldParseUnknownTypeCodeAsUnknown() {
        assertThat(AcknowledgementTypeCode.parse("AX")).isEqualTo(AcknowledgementTypeCode.UNKNOWN);
    }
}