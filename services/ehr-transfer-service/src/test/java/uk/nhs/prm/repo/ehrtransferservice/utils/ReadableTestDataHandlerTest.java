package uk.nhs.prm.repo.ehrtransferservice.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReadableTestDataHandlerTest {

    private TestDataLoader rawLoader = new TestDataLoader();
    private ReadableTestDataHandler readableHandler = new ReadableTestDataHandler();

    @Test
    void splitMessageShouldCreateReadableXmlEnvelopeFromMessage_soThatItCanBeUsedToMakeTestDataReadable() throws Exception {
        readableHandler.splitMessage("readabletestdataXReadableTestDataHandlerTestData");

        var readableEnvelope = rawLoader.getDataAsString("readable/readabletestdataX/ReadableTestDataHandlerTestData/envelope.xml");
        assertThat(readableEnvelope).contains("</soap:Envelope>");

        var readableContent = rawLoader.getDataAsString("readable/readabletestdataX/ReadableTestDataHandlerTestData/payload.xml");
        assertThat(readableContent).contains("<hl7:qualifier code=\"ER\"/>");
    }

    @Test
    void readMessageShouldCreateMessageBodyFromReadablePartsThatIsIdenticalToTheUnreadableOriginal() throws Exception {
        var unreadableMessage = rawLoader.getDataAsString("readabletestdataXReadableTestDataHandlerTestData");

        var messageFromReadableLoader = readableHandler.readMessage("readabletestdataX", "ReadableTestDataHandlerTestData");

        assertThat(messageFromReadableLoader).isEqualTo(unreadableMessage);
    }
}