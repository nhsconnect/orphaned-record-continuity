package uk.nhs.prm.repo.re_registration.parser;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ActiveSuspensionsParserTest {

    private final static String NHS_NUMBER = "some-nhs-number";
    private final static String PREVIOUS_ODS_CODE = "some-ods-code";
    private final static String NEMS_LAST_UPDATED_DATE = "last-updated";

    @Test
    public void shouldParseActiveSuspensionsMessage() {
        var activeSuspensionsMessage = "{\"nhsNumber\":\"" + NHS_NUMBER + "\",\"previousOdsCode\":\"" + PREVIOUS_ODS_CODE + "\", \"nemsLastUpdatedDate\":\"" + NEMS_LAST_UPDATED_DATE + "\"}";
        var parser = new ActiveSuspensionsParser();

        var parsedMessage = parser.parse(activeSuspensionsMessage);

        assertThat(parsedMessage.getNhsNumber()).isEqualTo(NHS_NUMBER);
        assertThat(parsedMessage.getPreviousOdsCode()).isEqualTo(PREVIOUS_ODS_CODE);
        assertThat(parsedMessage.getNemsLastUpdatedDate()).isEqualTo(NEMS_LAST_UPDATED_DATE);
    }
}