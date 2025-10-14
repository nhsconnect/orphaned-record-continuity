package uk.nhs.prm.repo.re_registration.parser;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReRegistrationParserTest {

    private final static String NHS_NUMBER = "some-nhs-number";
    private final static String NEWLY_REGISTERED_ODS_CODE = "some-ods-code";
    private final static String NEMS_MESSAGE_ID = "some-nems-message-id";
    private final static String LAST_UPDATED = "last-updated";

    @Test
    void shouldParseReRegistrationMessageCorrectlyWhenAMessageContainsExpectedValues() {

        var reRegistrationMessage = "{\"nhsNumber\":\"" + NHS_NUMBER + "\",\"newlyRegisteredOdsCode\":\"" + NEWLY_REGISTERED_ODS_CODE + "\", \"nemsMessageId\":\"" + NEMS_MESSAGE_ID + "\",\"lastUpdated\":\"" + LAST_UPDATED + "\"}";

        var reRegistrationParser = new ReRegistrationParser();
        var parsedMessage = reRegistrationParser.parse(reRegistrationMessage);

        assertEquals(NHS_NUMBER, parsedMessage.getNhsNumber());
        assertEquals(NEWLY_REGISTERED_ODS_CODE, parsedMessage.getNewlyRegisteredOdsCode());
        assertEquals(NEMS_MESSAGE_ID, parsedMessage.getNemsMessageId());
        assertEquals(LAST_UPDATED, parsedMessage.getLastUpdated());
    }
}