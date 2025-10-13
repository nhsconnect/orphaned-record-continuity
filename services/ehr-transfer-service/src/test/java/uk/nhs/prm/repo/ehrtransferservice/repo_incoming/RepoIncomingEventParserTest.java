package uk.nhs.prm.repo.ehrtransferservice.repo_incoming;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


class RepoIncomingEventParserTest {

    @Test
    void shouldParseRepoIncomingMessageCorrectlyWhenAMessageContainsExpectedValues() {
        String incomingMessage = "{\"nhsNumber\":\"nhs-number\"," +
                "\"sourceGp\":\"source-gp\"," +
                "\"nemsMessageId\":\"nems-message-id\"," +
                "\"destinationGp\":\"destination-GP\"," +
                "\"nemsEventLastUpdated\":\"last-updated\"}";
        var repoIncomingEventParser = new RepoIncomingEventParser();
        var parsedMessage = repoIncomingEventParser.parse(incomingMessage);
        assertEquals("nhs-number", parsedMessage.getNhsNumber());
    }

    @Test
    void shouldThrowAnExceptionWhenItTriesToParseAGarbageMessage() {
        String incomingMessage = "invalid";
        var repoIncomingEventParser = new RepoIncomingEventParser();
        assertThrows(RuntimeException.class, () -> repoIncomingEventParser.parse(incomingMessage));
    }
}