package uk.nhs.prm.repo.suspension.service.suspensionsevents;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class SuspensionEventParserTest {

    @Test
    void parseShouldThrowAnExceptionWhenMessageIsInvalid() {
        var parser = new SuspensionEventParser();
        assertThrows(JsonProcessingException.class, () -> parser.parse("invalid message"));
    }
}