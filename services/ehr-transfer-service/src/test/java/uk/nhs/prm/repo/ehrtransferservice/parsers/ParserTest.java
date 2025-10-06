package uk.nhs.prm.repo.ehrtransferservice.parsers;

import com.fasterxml.jackson.core.JsonParseException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.ParsedMessage;
import uk.nhs.prm.repo.ehrtransferservice.utils.ReadableTestDataHandler;
import uk.nhs.prm.repo.ehrtransferservice.utils.TestDataLoader;

import java.io.IOException;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("unit")
class ParserTest {

    private final Parser parser = new Parser();
    private final TestDataLoader rawLoader = new TestDataLoader();
    private final ReadableTestDataHandler readableReader = new ReadableTestDataHandler();

    @ParameterizedTest
    @CsvSource({
            "RCMR_IN010000UK05, MessageBody",
            "RCMR_IN030000UK06, MessageBody",
            "PRPA_IN000202UK01, MessageBody"
    })
    void shouldExtractActionNameFromMessageBodyMessage(String interactionId, String variant) throws IOException {
        String messageAsString = readableReader.readMessage(interactionId, variant);
        ParsedMessage parsedMessage = parser.parse(messageAsString);

        assertThat(parsedMessage.getInteractionId(), equalTo(interactionId));
    }

    @ParameterizedTest
    @CsvSource({
            "RCMR_IN030000UK06, WithMidMessageBody, true",
            "RCMR_IN030000UK06, MessageBody, false"
    })
    void shouldCheckIfMessageIsLarge(String interactionId, String variant, boolean isLargeMessage) throws IOException {
        String messageAsString = readableReader.readMessage(interactionId, variant);
        ParsedMessage parsedMessage = parser.parse(messageAsString);

        assertThat(parsedMessage.isLargeMessage(), equalTo(isLargeMessage));
    }

    @ParameterizedTest
    @CsvSource({
            "RCMR_IN010000UK05, MessageBody, 17a757f2-f4d2-444e-a246-9cb77bef7f22",
            "RCMR_IN030000UK06, MessageBody, ff27abc3-9730-40f7-ba82-382152e6b90a",
            "PRPA_IN000202UK01, MessageBody, 723c5f3a-1ab8-4515-a582-3e5cc600bf59"
    })
    void shouldExtractConversationIdFromMessageBodyMessage(String interactionId, String variant, UUID expectedConversationId) throws IOException {
        String messageAsString = readableReader.readMessage(interactionId, variant);
        ParsedMessage parsedMessage = parser.parse(messageAsString);

        assertThat(parsedMessage.getConversationId(), equalTo(expectedConversationId));
    }

    @ParameterizedTest
    @CsvSource({
            "RCMR_IN010000UK05, MessageBody, C445C720-B0EB-4E36-AF8A-48CD1CA5DE4F",
            "RCMR_IN030000UK06, MessageBody, 1C66BB0E-811E-4956-8F9C-33424695B75F",
            "PRPA_IN000202UK01, MessageBody, 11F4D7DF-EB49-45A5-A310-59FFFCF98C2A"
    })
    void shouldExtractMessageIdFromMessageBodyMessage(String interactionId, String variant, UUID expectedMessageId) throws IOException {
        String messageAsString = readableReader.readMessage(interactionId, variant);
        ParsedMessage parsedMessage = parser.parse(messageAsString);

        assertThat(parsedMessage.getMessageId(), equalTo(expectedMessageId));
    }

    @Test
    void shouldExtractNhsNumberFromEhrExtract() throws IOException {
        String fileName = "RCMR_IN030000UK06";
        String messageAsString = rawLoader.getDataAsString(fileName);
        ParsedMessage parsedMessage = parser.parse(messageAsString);

        assertThat(parsedMessage.getNhsNumber(), equalTo("9692842304"));
    }

    @Test
    void shouldParseMessageWithUnexpectedBackslashInNhsNumber() throws IOException {
        String messageAsString = readableReader.readMessage("RCMR_IN030000UK06", "MessageBodyWithUnexpectedBackslash");
        ParsedMessage parsedMessage = parser.parse(messageAsString);

        assertThat(parsedMessage.getNhsNumber(), equalTo("9692\\842304"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "simpleTextMessage.txt",
            "RCMR_IN030000UK06WithoutInteractionId",
            "RCMR_IN030000UK06WithoutMessageHeader",
            "RCMR_IN030000UK06WithoutSoapHeader",
            "RCMR_IN030000UK06WithIncorrectInteractionId"
    })
    void shouldThrowJsonParseExceptionWhenCannotParseMessage(String fileName) throws IOException {
        String messageAsString = rawLoader.getDataAsString(fileName);

        Exception expected = assertThrows(JsonParseException.class, () ->
                parser.parse(messageAsString)
        );
        assertThat(expected, notNullValue());
    }
}
