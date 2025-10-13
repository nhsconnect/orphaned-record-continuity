package uk.nhs.prm.repo.ehrtransferservice.models.ack;


import org.junit.jupiter.api.Test;
import uk.nhs.prm.repo.ehrtransferservice.models.ack.FailureLevel;

import static org.assertj.core.api.Assertions.assertThat;

class FailureLevelTest {

    @Test
    void shouldParseErrorFromCode() {
        assertThat(FailureLevel.parse("ER")).isEqualTo(FailureLevel.ERROR);
    }

    @Test
    void shouldParseWarningFromCode() {
        assertThat(FailureLevel.parse("WG")).isEqualTo(FailureLevel.WARNING);
    }

    @Test
    void shouldParseInformationalOnlyFromCode() {
        assertThat(FailureLevel.parse("IF")).isEqualTo(FailureLevel.INFO);
    }

    @Test
    void shouldParseAsUnknownIfNoCodePresent() {
        assertThat(FailureLevel.parse(null)).isEqualTo(FailureLevel.UNKNOWN);
    }

    @Test
    void shouldParseAsUnknownIfCodeIsEmpty() {
        assertThat(FailureLevel.parse("")).isEqualTo(FailureLevel.UNKNOWN);
    }

    @Test
    void shouldParseAsUnknownIfCodeIsUnexpectedOutOfSpecValueUsed() {
        assertThat(FailureLevel.parse("rather unexpected")).isEqualTo(FailureLevel.UNKNOWN);
    }
}