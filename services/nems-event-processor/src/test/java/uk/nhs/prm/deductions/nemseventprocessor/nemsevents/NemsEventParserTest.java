package uk.nhs.prm.deductions.nemseventprocessor.nemsevents;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NemsEventParserTest {
    private static final String PREVIOUS_GP_ORGANIZATION =
            "    <entry>\n" +
                    "        <fullUrl value=\"urn:uuid:e84bfc04-2d79-451e-84ef-a50116506088\"/>\n" +
                    "        <resource>\n" +
                    "            <Organization>\n" +
                    "                <identifier>\n" +
                    "                    <system value=\"https://fhir.nhs.uk/Id/ods-organization-code\"/>\n" +
                    "                    <value value=\"B85612\"/>\n" +
                    "                </identifier>\n" +
                    "            </Organization>\n" +
                    "        </resource>\n" +
                    "    </entry>";

    private static final String EPISODE_OF_CARE =
            "   <entry>\n" +
                    "        <resource>\n" +
                    "            <EpisodeOfCare>\n" +
                    "                <status value=\"finished\"/>\n" +
                    "                <managingOrganization>\n" +
                    "                    <reference value=\"urn:uuid:e84bfc04-2d79-451e-84ef-a50116506088\"/>\n" +
                    "                </managingOrganization>\n" +
                    "            </EpisodeOfCare>\n" +
                    "        </resource>\n" +
                    "    </entry>\n";

    public static final String MESSAGE_HEADERS = "   <entry>\n" +
            "        <resource>\n" +
            "            <MessageHeader>\n" +
            "                <id value=\"3cfdf880-13e9-4f6b-8299-53e96ef5ec02\"/>" +
            "                <meta>\n" +
            "                    <lastUpdated value=\"2017-11-01T15:00:33+00:00\"/>\n" +
            "                </meta>\n" +
            "            </MessageHeader>\n" +
            "        </resource>\n" +
            "    </entry>";

    public static final String PATIENT_ENTRY_WITHOUT_CURRENT_GP = "    <entry>\n" +
            "        <resource>\n" +
            "            <Patient>\n" +
            "                <identifier>\n" +
            "                   <extension>\n" +
            "                        <valueCodeableConcept>\n" +
            "                            <coding>\n" +
            "                                <code value=\"01\"/>\n" +
            "                            </coding>\n" +
            "                        </valueCodeableConcept>\n" +
            "                    </extension>" +
            "                    <value value=\"9912003888\"/>\n" +
            "                </identifier>\n" +
            "            </Patient>\n" +
            "        </resource>\n" +
            "    </entry>\n";

    public static final String PATIENT_ENTRY_WITH_CURRENT_GP = "    <entry>\n" +
            "        <resource>\n" +
            "            <Patient>\n" +
            "                <identifier>\n" +
            "                   <extension>\n" +
            "                        <valueCodeableConcept>\n" +
            "                            <coding>\n" +
            "                                <code value=\"01\"/>\n" +
            "                            </coding>\n" +
            "                        </valueCodeableConcept>\n" +
            "                    </extension>" +
            "                    <value value=\"9912003888\"/>\n" +
            "                </identifier>\n" +
            "                <generalPractitioner>\n" +
            "                    <reference value=\"urn:uuid:e84bfc04-2d79-451e-84ef-a50116506088\"/>\n" +
            "                    <display value=\"SHADWELL MEDICAL CENTRE\"/>\n" +
            "                </generalPractitioner>" +
            "            </Patient>\n" +
            "        </resource>\n" +
            "    </entry>\n";

    @Mock
    private NemsEventValidator nemsEventValidator;

    @InjectMocks
    private NemsEventParser nemsEventParser;

    @Test
    void shouldParseANemsMessageAsASuspensionWhenGPFieldIsMissing() {
        var messageBody = "<Bundle xmlns=\"http://hl7.org/fhir\">\n" +
                MESSAGE_HEADERS +
                PATIENT_ENTRY_WITHOUT_CURRENT_GP +
                EPISODE_OF_CARE +
                PREVIOUS_GP_ORGANIZATION +
                "</Bundle>";

        var message = nemsEventParser.parse(messageBody);

        assertTrue(message.isSuspension());
        verify(nemsEventValidator).validate("9912003888", "01", "B85612");
    }

    @Test
    void shouldParseANemsMessageAsAReRegistrationWhenGPFieldIsPresentInThePatientSectionAndEpisodeOfCareIsMissing() {
        var messageBody = "<Bundle xmlns=\"http://hl7.org/fhir\">\n" +
                MESSAGE_HEADERS +
                PATIENT_ENTRY_WITH_CURRENT_GP +
                PREVIOUS_GP_ORGANIZATION +
                "</Bundle>";

        var message = nemsEventParser.parse(messageBody);

        assertTrue(message.isReRegistration());
        verify(nemsEventValidator).validate("9912003888", "01", "B85612");
    }

    @Test
    void shouldParseANemsMessageAsANonSuspensionWhenGPFieldIsPresentInThePatientSection() {
        var messageBody = "<Bundle xmlns=\"http://hl7.org/fhir\">\n" +
                MESSAGE_HEADERS +
                PATIENT_ENTRY_WITH_CURRENT_GP +
                EPISODE_OF_CARE +
                PREVIOUS_GP_ORGANIZATION +
                "</Bundle>";

        var message = nemsEventParser.parse(messageBody);

        assertFalse(message.isSuspension());
        assertFalse(message.isReRegistration());
    }

    @Test
    void shouldParseNhsNumberFromANemsMessagePatientEntry() {
        var messageBody = "<Bundle xmlns=\"http://hl7.org/fhir\">\n" +
                MESSAGE_HEADERS +
                "    <entry>\n" +
                "        <resource>\n" +
                "            <Patient>\n" +
                "                <identifier>\n" +
                "                   <extension>\n" +
                "                       <valueCodeableConcept>\n" +
                "                            <coding>\n" +
                "                                <code value=\"01\"/>\n" +
                "                            </coding>\n" +
                "                        </valueCodeableConcept>\n" +
                "                    </extension>" +
                "                    <system value=\"https://fhir.nhs.uk/Id/nhs-number\"/>\n" +
                "                    <value value=\"9912003888\"/>\n" +
                "                </identifier>\n" +
                "            </Patient>\n" +
                "        </resource>\n" +
                "    </entry>\n" +
                EPISODE_OF_CARE +
                PREVIOUS_GP_ORGANIZATION +
                "</Bundle>";

        var message = nemsEventParser.parse(messageBody);

        assertThat(message.getNhsNumber()).isEqualTo("9912003888");
    }

    @Test
    void shouldParseANemsMessageAsASuspensionWhenGPFieldIsPresentOnlyInNonPatientEntriesInTheMessage() {
        var messageBody = "<Bundle xmlns=\"http://hl7.org/fhir\">\n" +
                MESSAGE_HEADERS +
                PATIENT_ENTRY_WITHOUT_CURRENT_GP +
                "    <entry>\n" +
                "        <resource>\n" +
                "            <Organization>\n" +
                "                <generalPractitioner>\n" +
                "                    <reference value=\"urn:uuid:59a63170-b769-44f7-acb1-1234abcd\"/>\n" +
                "                    <display value=\"SHUDEHILL MEDICAL CENTRE\"/>\n" +
                "                </generalPractitioner>\n" +
                "            </Organization>\n" +
                "        </resource>\n" +
                "    </entry>\n" +
                EPISODE_OF_CARE +
                PREVIOUS_GP_ORGANIZATION +
                "</Bundle>";

        var message = nemsEventParser.parse(messageBody);

        assertTrue(message.isSuspension());
    }

    @Test
    void shouldExtractLastUpdatedFieldWhenParsingASuspensionMessageSoThatItCanBeUsedToWorkOutTheLatestNemsEvent() {
        var messageBody = "<Bundle xmlns=\"http://hl7.org/fhir\">\n" +
                "   <entry>\n" +
                "        <resource>\n" +
                "            <MessageHeader>\n" +
                "                <id value=\"3cfdf880-13e9-4f6b-8299-53e96ef5ec02\"/>" +
                "                <meta>\n" +
                "                    <lastUpdated value=\"2017-11-01T15:00:33+00:00\"/>\n" +
                "                </meta>\n" +
                "                <timestamp value=\"2019-11-01T15:00:00+00:00\"/>\n" +
                "            </MessageHeader>\n" +
                "        </resource>\n" +
                "    </entry>" +
                PATIENT_ENTRY_WITHOUT_CURRENT_GP +
                EPISODE_OF_CARE +
                PREVIOUS_GP_ORGANIZATION +
                "</Bundle>";

        var message = nemsEventParser.parse(messageBody);

        assertTrue(message.isSuspension());
        assertThat(message.getLastUpdated()).isEqualTo("2017-11-01T15:00:33+00:00");
    }

    @Test
    void shouldExtractGPPracticeURLFieldWhenParsingASuspensionMessage() {
        var messageBody = "<Bundle xmlns=\"http://hl7.org/fhir\">\n" +
                MESSAGE_HEADERS +
                PATIENT_ENTRY_WITHOUT_CURRENT_GP +
                "   <entry>\n" +
                "        <resource>\n" +
                "            <EpisodeOfCare>\n" +
                "                <status value=\"finished\"/>\n" +
                "                <managingOrganization>\n" +
                "                    <reference value=\"urn:uuid:e84bfc04-2d79-451e-84ef-a50116506088\"/>\n" +
                "                </managingOrganization>\n" +
                "            </EpisodeOfCare>\n" +
                "        </resource>\n" +
                "    </entry>\n" +
                "    <entry>\n" +
                "        <fullUrl value=\"urn:uuid:e84bfc04-2d79-451e-84ef-a50116506088\"/>\n" +
                "        <resource>\n" +
                "            <Organization>\n" +
                "                <identifier>\n" +
                "                    <system value=\"https://fhir.nhs.uk/Id/ods-organization-code\"/>\n" +
                "                    <value value=\"B85612\"/>\n" +
                "                </identifier>\n" +
                "            </Organization>\n" +
                "        </resource>\n" +
                "    </entry>" +
                "</Bundle>";

        var message = nemsEventParser.parse(messageBody);

        assertTrue(message.isSuspension());
        assertThat(message.getOdsCode()).isEqualTo("B85612");
    }

    @Test
    void shouldExtractGPPracticeURLFieldWhenParsingAReRegistrationMessage() {
        var messageBody = "<Bundle xmlns=\"http://hl7.org/fhir\">\n" +
                MESSAGE_HEADERS +
                PATIENT_ENTRY_WITH_CURRENT_GP +
                "    <entry>\n" +
                "        <fullUrl value=\"urn:uuid:e84bfc04-2d79-451e-84ef-a50116506088\"/>\n" +
                "        <resource>\n" +
                "            <Organization>\n" +
                "                <identifier>\n" +
                "                    <system value=\"https://fhir.nhs.uk/Id/ods-organization-code\"/>\n" +
                "                    <value value=\"B85612\"/>\n" +
                "                </identifier>\n" +
                "            </Organization>\n" +
                "        </resource>\n" +
                "    </entry>" +
                "</Bundle>";

        var message = nemsEventParser.parse(messageBody);

        assertTrue(message.isReRegistration());
        assertThat(message.getOdsCode()).isEqualTo("B85612");
    }

    @Test
    void shouldExtractNemsMessageIdForASuspensionMessage() {
        var messageBody = "<Bundle xmlns=\"http://hl7.org/fhir\">\n" +
            MESSAGE_HEADERS +
            PATIENT_ENTRY_WITHOUT_CURRENT_GP +
            EPISODE_OF_CARE +
            PREVIOUS_GP_ORGANIZATION +
            "</Bundle>";

        var message = nemsEventParser.parse(messageBody);

        assertTrue(message.isSuspension());
        assertThat(message.getNemsMessageId()).isEqualTo("3cfdf880-13e9-4f6b-8299-53e96ef5ec02");
    }

    @Test
    void shouldExtractNemsMessageIdForANonSuspensionMessage() {
        var messageBody = "<Bundle xmlns=\"http://hl7.org/fhir\">\n" +
            MESSAGE_HEADERS +
            PATIENT_ENTRY_WITH_CURRENT_GP +
            EPISODE_OF_CARE +
            PREVIOUS_GP_ORGANIZATION +
            "</Bundle>";

        var message = nemsEventParser.parse(messageBody);

        assertFalse(message.isSuspension());
        assertThat(message.getNemsMessageId()).isEqualTo("3cfdf880-13e9-4f6b-8299-53e96ef5ec02");
    }

    @Test
    void shouldExtractMessageIdFromMessageHeaders() {
        var messageBody = "<Bundle xmlns=\"http://hl7.org/fhir\">\n" +
            MESSAGE_HEADERS +
            PATIENT_ENTRY_WITH_CURRENT_GP +
            EPISODE_OF_CARE +
            PREVIOUS_GP_ORGANIZATION +
            "</Bundle>";

        String actual = nemsEventParser.extractNemsMessageIdFromStringBody(messageBody);
        assertThat(actual).isEqualTo("3cfdf880-13e9-4f6b-8299-53e96ef5ec02");
    }

    //ERROR CASES

    @Test
    void shouldThrowAParseExceptionWhenInvalidXML() {
        assertThrows(NemsEventParseException.class, () -> {
            nemsEventParser.parse("<anyOldMessage></anyOldMessage>");
        });
    }

    @Test
    void shouldThrowParsingExceptionWhenPassedNonXml() {
        NemsEventParseException nemsEventParseException = assertThrows(NemsEventParseException.class, () -> {
            nemsEventParser.parse("not-an-xml");
        });

        assertThat(nemsEventParseException.getMessage()).contains("non XML message");
    }

    @Test
    void shouldFailToParseIfThereIsMoreThanOnePatientEntry() {
        var messageBody = "<Bundle xmlns=\"http://hl7.org/fhir\">\n" +
                MESSAGE_HEADERS +
                PATIENT_ENTRY_WITHOUT_CURRENT_GP +
                PATIENT_ENTRY_WITHOUT_CURRENT_GP +
                EPISODE_OF_CARE +
                PREVIOUS_GP_ORGANIZATION +
                "</Bundle>";

        assertThrows(NemsEventParseException.class, () -> nemsEventParser.parse(messageBody));
    }

    @Test
    void shouldThrowAnErrorWhenCannotExtractNhsNumberFromNemsEvent() {
        var messageBody = "<Bundle xmlns=\"http://hl7.org/fhir\">\n" +
                MESSAGE_HEADERS +
                "    <entry>\n" +
                "        <resource>\n" +
                "            <Patient>\n" +
                "                <identifier>\n" +
                "                </identifier>\n" +
                "            </Patient>\n" +
                "        </resource>\n" +
                "    </entry>\n" +
                EPISODE_OF_CARE +
                PREVIOUS_GP_ORGANIZATION +
                "</Bundle>";

        var nemsEventParseException = assertThrows(NemsEventParseException.class, () -> {
            nemsEventParser.parse(messageBody);
        });

        assertThat(nemsEventParseException.getMessage()).contains("NemsEventParseException: Cannot extract NHS Number from Patient Details Entry");
    }

    @Test
    void shouldThrowAnErrorWhenCannotExtractPreviousGpUrlFromNemsEvent() {
        var messageBody = "<Bundle xmlns=\"http://hl7.org/fhir\">\n" +
                MESSAGE_HEADERS +
                PATIENT_ENTRY_WITHOUT_CURRENT_GP +
                "   <entry>\n" +
                "        <resource>\n" +
                "            <EpisodeOfCare>\n" +
                "                <status value=\"finished\"/>\n" +
                "                <managingOrganization>\n" +
                "                </managingOrganization>\n" +
                "            </EpisodeOfCare>\n" +
                "        </resource>\n" +
                "    </entry>\n" +
                PREVIOUS_GP_ORGANIZATION +
                "</Bundle>";

        var nemsEventParseException = assertThrows(NemsEventParseException.class, () -> {
            nemsEventParser.parse(messageBody);
        });

        assertThat(nemsEventParseException.getMessage()).contains("NemsEventParseException: Cannot extract previous GP URL Field from finished EpisodeOfCare");
    }

    @Test
    void shouldThrowAnErrorWhenCannotExtractNewGpUrlFromNemsEvent() {
        var messageBody = "<Bundle xmlns=\"http://hl7.org/fhir\">\n" +
                MESSAGE_HEADERS +
                "    <entry>\n" +
                "        <resource>\n" +
                "            <Patient>\n" +
                "                <identifier>\n" +
                "                   <extension>\n" +
                "                        <valueCodeableConcept>\n" +
                "                            <coding>\n" +
                "                                <code value=\"01\"/>\n" +
                "                            </coding>\n" +
                "                        </valueCodeableConcept>\n" +
                "                    </extension>" +
                "                    <value value=\"9912003888\"/>\n" +
                "                </identifier>\n" +
                "                <generalPractitioner>\n" +
                "                </generalPractitioner>" +
                "            </Patient>\n" +
                "        </resource>\n" +
                "    </entry>\n" +
                "</Bundle>";

        var nemsEventParseException = assertThrows(NemsEventParseException.class, () -> {
            nemsEventParser.parse(messageBody);
        });

        assertThat(nemsEventParseException.getMessage()).contains("NemsEventParseException: Cannot extract new GP URL Field from generalPractitioner");
    }

    @Test
    void shouldThrowAnErrorWhenCannotExtractFinishedEpisodeOfCare() {
        var messageBody = "<Bundle xmlns=\"http://hl7.org/fhir\">\n" +
                MESSAGE_HEADERS +
                PATIENT_ENTRY_WITHOUT_CURRENT_GP +
                "   <entry>\n" +
                "        <resource>\n" +
                "            <EpisodeOfCare>\n" +
                "                <managingOrganization>\n" +
                "                    <reference value=\"urn:uuid:e84bfc04-2d79-451e-84ef-a50116506088\"/>\n" +
                "                </managingOrganization>\n" +
                "            </EpisodeOfCare>\n" +
                "        </resource>\n" +
                "    </entry>\n" +
                PREVIOUS_GP_ORGANIZATION +
                "</Bundle>";

        var nemsEventParseException = assertThrows(NemsEventParseException.class, () -> {
            nemsEventParser.parse(messageBody);
        });

        assertThat(nemsEventParseException.getMessage()).contains("NemsEventParseException: Cannot find EpisodeOfCare with finished status");
    }

    @Test
    void shouldThrowAnErrorWhenCannotExtractPreviousGpOdsCodeFromNemsEvent() {
        var messageBody = "<Bundle xmlns=\"http://hl7.org/fhir\">\n" +
            MESSAGE_HEADERS +
                PATIENT_ENTRY_WITHOUT_CURRENT_GP +
                "   <entry>\n" +
                "        <resource>\n" +
                "            <EpisodeOfCare>\n" +
                "                <status value=\"finished\"/>\n" +
                "                <managingOrganization>\n" +
                "                    <reference value=\"urn:uuid:e84bfc04-2d79-451e-84ef-a50116506088\"/>\n" +
                "                </managingOrganization>\n" +
                "            </EpisodeOfCare>\n" +
                "        </resource>\n" +
                "    </entry>\n" +
                "    <entry>\n" +
                "        <fullUrl value=\"urn:uuid:e84bfc04-2d79-451e-84ef-a50116506088\"/>\n" +
                "        <resource>\n" +
                "            <Organization>\n" +
                "            </Organization>\n" +
                "        </resource>\n" +
                "    </entry>" +
                "</Bundle>";

        var nemsEventParseException = assertThrows(NemsEventParseException.class, () -> {
            nemsEventParser.parse(messageBody);
        });

        assertThat(nemsEventParseException.getMessage()).contains("NemsEventParseException: Cannot extract previous GP ODS Code");
    }

    @Test
    void shouldThrowAnErrorWhenCannotExtractLastUpdatedValueFromNemsEvent() {
        var messageBody = "<Bundle xmlns=\"http://hl7.org/fhir\">\n" +
                "   <entry>\n" +
                "        <resource>\n" +
                "            <MessageHeader>\n" +
                "                <id value=\"3cfdf880-13e9-4f6b-8299-53e96ef5ec02\"/>" +
                "                <meta>\n" +
                "                </meta>\n" +
                "            </MessageHeader>\n" +
                "        </resource>\n" +
                "    </entry>" +
                PATIENT_ENTRY_WITHOUT_CURRENT_GP +
                EPISODE_OF_CARE +
                PREVIOUS_GP_ORGANIZATION +
                "</Bundle>";

        var nemsEventParseException = assertThrows(NemsEventParseException.class, () -> {
            nemsEventParser.parse(messageBody);
        });

        assertThat(nemsEventParseException.getMessage()).contains("NemsEventParseException: Cannot extract last updated field from Message Header Entry");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "2022-03-08 15:45:14.668751", "NOT-A-DATE"})
    void shouldThrowAnErrorWhenLastUpdatedValueIsNotFormattedAsIso8601Date(String wrongDateOrFormat) {
        var messageBody = "<Bundle xmlns=\"http://hl7.org/fhir\">\n" +
                "   <entry>\n" +
                "        <resource>\n" +
                "            <MessageHeader>\n" +
                "                <id value=\"3cfdf880-13e9-4f6b-8299-53e96ef5ec02\"/>" +
                "                <meta>\n" +
                "                    <lastUpdated value=\"" + wrongDateOrFormat + "\"/>\n" +
                "                </meta>\n" +
                "            </MessageHeader>\n" +
                "        </resource>\n" +
                "    </entry>" +
                PATIENT_ENTRY_WITHOUT_CURRENT_GP +
                EPISODE_OF_CARE +
                PREVIOUS_GP_ORGANIZATION +
                "</Bundle>";

        var nemsEventParseException = assertThrows(NemsEventParseException.class, () -> {
            nemsEventParser.parse(messageBody);
        });

        assertThat(nemsEventParseException.getMessage()).contains("NemsEventParseException: Cannot extract last updated field from Message Header Entry");
    }

    @Test
    void shouldThrowAnErrorWhenCannotExtractNemsEventIdValueFromNemsEvent() {
        var messageBody = "<Bundle xmlns=\"http://hl7.org/fhir\">\n" +
            "   <entry>\n" +
            "        <resource>\n" +
            "            <MessageHeader>\n" +
            "                <meta>\n" +
            "                    <lastUpdated value=\"2017-11-01T15:00:33+00:00\"/>\n" +
            "                </meta>\n" +
            "            </MessageHeader>\n" +
            "        </resource>\n" +
            "    </entry>" +
            PATIENT_ENTRY_WITHOUT_CURRENT_GP +
            EPISODE_OF_CARE +
            PREVIOUS_GP_ORGANIZATION +
            "</Bundle>";

        var nemsEventParseException = assertThrows(NemsEventParseException.class, () -> {
            nemsEventParser.parse(messageBody);
        });

        assertThat(nemsEventParseException.getMessage()).contains("NemsEventParseException: Cannot extract nems message id from Message Header Entry");
    }

    @Test
    void shouldThrowAnErrorWhenCannotFindMatchingGpUrl(){
        var messageBody = "<Bundle xmlns=\"http://hl7.org/fhir\">\n" +
            MESSAGE_HEADERS +
                PATIENT_ENTRY_WITHOUT_CURRENT_GP +
                "   <entry>\n" +
                "        <resource>\n" +
                "            <EpisodeOfCare>\n" +
                "                <status value=\"finished\"/>\n" +
                "                <managingOrganization>\n" +
                "                    <reference value=\"urn:uuid:e84bfc04-2d79-451e-84ef-a50116506088\"/>\n" +
                "                </managingOrganization>\n" +
                "            </EpisodeOfCare>\n" +
                "        </resource>\n" +
                "    </entry>\n" +
                "</Bundle>";

        var nemsEventParseException = assertThrows(NemsEventParseException.class, () -> {
            nemsEventParser.parse(messageBody);
        });

        assertThat(nemsEventParseException.getMessage()).contains("NemsEventParseException: Cannot find entry for Organization with previous GP ODS code");
    }

    @Test
    void shouldThrowAnErrorWhenCannotExtractNhsNumberVerificationValue(){
        var messageBody = "<Bundle xmlns=\"http://hl7.org/fhir\">\n" +
            MESSAGE_HEADERS +
                PREVIOUS_GP_ORGANIZATION +
                EPISODE_OF_CARE +
                "    <entry>\n" +
                "        <resource>\n" +
                "            <Patient>\n" +
                "                <identifier>\n" +
                "                   <extension>\n" +
                "                        <valueCodeableConcept>\n" +
                "                        </valueCodeableConcept>\n" +
                "                    </extension>" +
                "                    <value value=\"9912003888\"/>\n" +
                "                </identifier>\n" +
                "            </Patient>\n" +
                "        </resource>\n" +
                "    </entry>\n" +
                " </Bundle>";

        var nemsEventParseException = assertThrows(NemsEventParseException.class, () -> {
            nemsEventParser.parse(messageBody);
        });

        assertThat(nemsEventParseException.getMessage()).contains("NemsEventParseException: Cannot extract nhs number verification value from Patient Details Entry");
    }

    @Test
    void shouldThrowAnErrorWhenCannotExtractPatientFromNemsEvent() {
        var messageBody = "<Bundle xmlns=\"http://hl7.org/fhir\">\n" +
            MESSAGE_HEADERS +
                EPISODE_OF_CARE +
                PREVIOUS_GP_ORGANIZATION +
                "</Bundle>";

        var nemsEventParseException = assertThrows(NemsEventParseException.class, () -> {
            nemsEventParser.parse(messageBody);
        });

        assertThat(nemsEventParseException.getMessage()).contains("NemsEventParseException: Cannot find Patient Details entry - invalid message");
    }

    @Test
    void shouldThrowAParseExceptionWhenInvalidXMLWhenExtractingNemsId() {
        assertThrows(NemsEventParseException.class, () -> {
            nemsEventParser.extractNemsMessageIdFromStringBody("<anyOldMessage></anyOldMessage>");
        });
    }

    @Test
    void shouldThrowAnErrorWhenCannotExtractNemsMessageId() {
        var messageBody = "<Bundle xmlns=\"http://hl7.org/fhir\">\n" +
            "   <entry>\n" +
            "        <resource>\n" +
            "            <MessageHeader>\n" +
            "            </MessageHeader>\n" +
            "        </resource>\n" +
            "    </entry>" +
            "</Bundle>";

        NemsEventParseException nemsEventParseException = assertThrows(NemsEventParseException.class, () -> {
            nemsEventParser.extractNemsMessageIdFromStringBody(messageBody);
        });

        assertThat(nemsEventParseException.getMessage()).contains("Cannot extract nems message id from Message Header Entry");
    }
}
