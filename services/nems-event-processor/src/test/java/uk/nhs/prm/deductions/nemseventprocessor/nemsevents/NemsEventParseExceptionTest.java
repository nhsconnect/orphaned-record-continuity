package uk.nhs.prm.deductions.nemseventprocessor.nemsevents;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NemsEventParseExceptionTest {

    @Test
    void shouldShowItsANemsEventParsingExceptionInMessage() {
        NemsEventParseException nemsEventParseException = new NemsEventParseException("some-error");
        assertThat(nemsEventParseException.getMessage()).contains("NemsEventParseException: some-error");
    }
}