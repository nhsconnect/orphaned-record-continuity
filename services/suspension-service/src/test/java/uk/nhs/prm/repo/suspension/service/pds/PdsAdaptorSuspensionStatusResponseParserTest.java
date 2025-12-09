package uk.nhs.prm.repo.suspension.service.pds;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;


class PdsAdaptorSuspensionStatusResponseParserTest {

    private PdsAdaptorSuspensionStatusResponseParser parser;

    @BeforeEach
    public void setUp() {
        parser = new PdsAdaptorSuspensionStatusResponseParser();
    }

    @Test
    public void shouldParseStatusOfARegisteredPatientCorrectly() {
        String nonSuspendedResponseBody = "{\n" +
                "    \"nhsNumber\": \"1234567890\",\n" +
                "    \"isSuspended\": false,\n" +
                "    \"currentOdsCode\": \"B86041\",\n" +
                "    \"managingOrganisation\": null,\n" +
                "    \"recordETag\": \"bob\"\n" +
                "}";

        var status = parser.parse(nonSuspendedResponseBody);

        assertThat(status.getIsSuspended()).isFalse();
        assertThat(status.getCurrentOdsCode()).isEqualTo("B86041");
        assertThat(status.getManagingOrganisation()).isNull();
        assertThat(status.getRecordETag()).isEqualTo("bob");
    }

    @Test
    public void shouldParseStatusOfASuspendedPatient() {
        String suspendedResponseBody = "{\n" +
                "    \"nhsNumber\": \"1234567890\",\n" +
                "    \"isSuspended\": true,\n" +
                "    \"currentOdsCode\": null,\n" +
                "    \"managingOrganisation\": \"B86042\",\n" +
                "    \"recordETag\": \"foo\"\n" +
                "}";

        var status = parser.parse(suspendedResponseBody);

        assertThat(status.getIsSuspended()).isTrue();
        assertThat(status.getCurrentOdsCode()).isNull();
        assertThat(status.getManagingOrganisation()).isEqualTo("B86042");
        assertThat(status.getRecordETag()).isEqualTo("foo");
    }

    @Test
    public void shouldParseStatusOfASuspendedPatientWithNullMOF() {
        String suspendedResponseBody = "{\n" +
                "    \"nhsNumber\": \"1234567890\",\n" +
                "    \"isSuspended\": true,\n" +
                "    \"currentOdsCode\": null,\n" +
                "    \"managingOrganisation\": null,\n" +
                "    \"recordETag\": \"foo\"\n" +
                "}";

        var status = parser.parse(suspendedResponseBody);

        assertThat(status.getIsSuspended()).isTrue();
        assertThat(status.getCurrentOdsCode()).isNull();
        assertThat(status.getManagingOrganisation()).isNull();
        assertThat(status.getRecordETag()).isEqualTo("foo");
    }

    @Test
    public void shouldParseStatusOfADeceasedPatient() {
        String suspendedResponseBody = "{\n" +
                "    \"nhsNumber\": \"1234567890\",\n" +
                "    \"isSuspended\": null,\n" +
                "    \"currentOdsCode\": null,\n" +
                "    \"managingOrganisation\": null,\n" +
                "    \"recordETag\": \"foo\",\n" +
                "    \"isDeceased\": true \n" +
                "}";

        var status = parser.parse(suspendedResponseBody);

        assertThat(status.getIsSuspended()).isNull();
        assertThat(status.getCurrentOdsCode()).isNull();
        assertThat(status.getManagingOrganisation()).isNull();
        assertThat(status.getRecordETag()).isEqualTo("foo");
        assertThat(status.getIsDeceased()).isTrue();
    }

    @Test
    public void shouldThrowIfThereIsNoResponseBodyToParse() {
        assertThrows(UnexpectedPdsAdaptorResponseException.class, () -> parser.parse(null));
    }

    @Test
    public void shouldThrowIfTheResponseBodyIsMalformed() {
        assertThrows(UnexpectedPdsAdaptorResponseException.class, () -> parser.parse("not actually json"));
    }

    @Test
    public void shouldThrowIfTheResponseBodyIsEmpty() {
        assertThrows(UnexpectedPdsAdaptorResponseException.class, () -> parser.parse(""));
    }
}