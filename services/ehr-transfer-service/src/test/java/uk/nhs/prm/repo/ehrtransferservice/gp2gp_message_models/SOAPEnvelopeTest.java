package uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.core.IsEqual.equalTo;

@Tag("unit")
class SOAPEnvelopeTest {

    @Test
    void shouldPopulateActionInEnvelope() throws JsonProcessingException {
        String envelopeText = "\n" +
                "    <soap:Envelope xmlns:eb=\"http://www.oasis-open.org/committees/ebxml-msg/schema/msg-header-2_0.xsd\"\n" +
                "                   xmlns:hl7ebxml=\"urn:hl7-org:transport/ebxml/DSTUv1.0\"\n" +
                "                   xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
                "        <soap:Header>\n" +
                "            <eb:MessageHeader eb:version=\"2.0\" soap:mustUnderstand=\"1\">\n" +
                "                <eb:From>\n" +
                "                    <eb:PartyId eb:type=\"urn:nhs:names:partyType:ocs+serviceInstance\">5XZ-821385</eb:PartyId>\n" +
                "                </eb:From>\n" +
                "                <eb:To>\n" +
                "                    <eb:PartyId eb:type=\"urn:nhs:names:partyType:ocs+serviceInstance\">B86041-822103</eb:PartyId>\n" +
                "                </eb:To>\n" +
                "                <eb:CPAId>e06af803674408a9d8e8</eb:CPAId>\n" +
                "                <eb:ConversationId>8B373671-5884-45DF-A22C-B3EF768E1DC4</eb:ConversationId>\n" +
                "                <eb:Service>urn:nhs:names:services:gp2gp</eb:Service>\n" +
                "                <eb:Action>RCMR_IN030000UK06</eb:Action>\n" +
                "                <eb:MessageData>\n" +
                "                    <eb:MessageId>72EAA355-B152-4B24-A088-AC2F66AE8A21</eb:MessageId>\n" +
                "                    <eb:Timestamp>2021-03-09T14:21:22.646Z</eb:Timestamp>\n" +
                "                    <eb:TimeToLive>2021-03-09T20:36:22.646Z</eb:TimeToLive>\n" +
                "                </eb:MessageData>\n" +
                "                <eb:DuplicateElimination/>\n" +
                "            </eb:MessageHeader>\n" +
                "            <eb:AckRequested eb:version=\"2.0\" soap:mustUnderstand=\"1\"\n" +
                "                             soap:actor=\"urn:oasis:names:tc:ebxml-msg:actor:nextMSH\" eb:signed=\"false\"/>\n" +
                "        </soap:Header>\n" +
                "    </soap:Envelope>";
        XmlMapper xmlMapper = new XmlMapper();
        SOAPEnvelope envelope = xmlMapper.readValue(envelopeText, SOAPEnvelope.class);
        assertThat(envelope.header.messageHeader.action, equalTo("RCMR_IN030000UK06"));
    }

    @Test
    void shouldPopulateConversationIdInEnvelope() throws JsonProcessingException {
        String envelopeText = "\n" +
                "    <soap:Envelope xmlns:eb=\"http://www.oasis-open.org/committees/ebxml-msg/schema/msg-header-2_0.xsd\"\n" +
                "                   xmlns:hl7ebxml=\"urn:hl7-org:transport/ebxml/DSTUv1.0\"\n" +
                "                   xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
                "        <soap:Header>\n" +
                "            <eb:MessageHeader eb:version=\"2.0\" soap:mustUnderstand=\"1\">\n" +
                "                <eb:From>\n" +
                "                    <eb:PartyId eb:type=\"urn:nhs:names:partyType:ocs+serviceInstance\">5XZ-821385</eb:PartyId>\n" +
                "                </eb:From>\n" +
                "                <eb:To>\n" +
                "                    <eb:PartyId eb:type=\"urn:nhs:names:partyType:ocs+serviceInstance\">B86041-822103</eb:PartyId>\n" +
                "                </eb:To>\n" +
                "                <eb:CPAId>e06af803674408a9d8e8</eb:CPAId>\n" +
                "                <eb:ConversationId>8B373671-5884-45DF-A22C-B3EF768E1DC4</eb:ConversationId>\n" +
                "                <eb:Service>urn:nhs:names:services:gp2gp</eb:Service>\n" +
                "                <eb:Action>RCMR_IN030000UK06</eb:Action>\n" +
                "                <eb:MessageData>\n" +
                "                    <eb:MessageId>72EAA355-B152-4B24-A088-AC2F66AE8A21</eb:MessageId>\n" +
                "                    <eb:Timestamp>2021-03-09T14:21:22.646Z</eb:Timestamp>\n" +
                "                    <eb:TimeToLive>2021-03-09T20:36:22.646Z</eb:TimeToLive>\n" +
                "                </eb:MessageData>\n" +
                "                <eb:DuplicateElimination/>\n" +
                "            </eb:MessageHeader>\n" +
                "            <eb:AckRequested eb:version=\"2.0\" soap:mustUnderstand=\"1\"\n" +
                "                             soap:actor=\"urn:oasis:names:tc:ebxml-msg:actor:nextMSH\" eb:signed=\"false\"/>\n" +
                "        </soap:Header>\n" +
                "    </soap:Envelope>";
        XmlMapper xmlMapper = new XmlMapper();
        SOAPEnvelope envelope = xmlMapper.readValue(envelopeText, SOAPEnvelope.class);
        assertThat(envelope.header.messageHeader.conversationId, equalTo(UUID.fromString("8B373671-5884-45DF-A22C-B3EF768E1DC4")));
    }

    @Test
    void shouldPopulateMessageIdInEnvelope() throws JsonProcessingException {
        String envelopeText = "\n" +
                "    <soap:Envelope xmlns:eb=\"http://www.oasis-open.org/committees/ebxml-msg/schema/msg-header-2_0.xsd\"\n" +
                "                   xmlns:hl7ebxml=\"urn:hl7-org:transport/ebxml/DSTUv1.0\"\n" +
                "                   xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
                "        <soap:Header>\n" +
                "            <eb:MessageHeader eb:version=\"2.0\" soap:mustUnderstand=\"1\">\n" +
                "                <eb:From>\n" +
                "                    <eb:PartyId eb:type=\"urn:nhs:names:partyType:ocs+serviceInstance\">5XZ-821385</eb:PartyId>\n" +
                "                </eb:From>\n" +
                "                <eb:To>\n" +
                "                    <eb:PartyId eb:type=\"urn:nhs:names:partyType:ocs+serviceInstance\">B86041-822103</eb:PartyId>\n" +
                "                </eb:To>\n" +
                "                <eb:CPAId>e06af803674408a9d8e8</eb:CPAId>\n" +
                "                <eb:ConversationId>8B373671-5884-45DF-A22C-B3EF768E1DC4</eb:ConversationId>\n" +
                "                <eb:Service>urn:nhs:names:services:gp2gp</eb:Service>\n" +
                "                <eb:Action>RCMR_IN030000UK06</eb:Action>\n" +
                "                <eb:MessageData>\n" +
                "                    <eb:MessageId>72EAA355-B152-4B24-A088-AC2F66AE8A21</eb:MessageId>\n" +
                "                    <eb:Timestamp>2021-03-09T14:21:22.646Z</eb:Timestamp>\n" +
                "                    <eb:TimeToLive>2021-03-09T20:36:22.646Z</eb:TimeToLive>\n" +
                "                </eb:MessageData>\n" +
                "                <eb:DuplicateElimination/>\n" +
                "            </eb:MessageHeader>\n" +
                "            <eb:AckRequested eb:version=\"2.0\" soap:mustUnderstand=\"1\"\n" +
                "                             soap:actor=\"urn:oasis:names:tc:ebxml-msg:actor:nextMSH\" eb:signed=\"false\"/>\n" +
                "        </soap:Header>\n" +
                "    </soap:Envelope>";
        XmlMapper xmlMapper = new XmlMapper();
        SOAPEnvelope envelope = xmlMapper.readValue(envelopeText, SOAPEnvelope.class);
        assertThat(envelope.header.messageHeader.messageData.messageId, equalTo(UUID.fromString("72EAA355-B152-4B24-A088-AC2F66AE8A21")));
    }

    @Test
    void shouldPopulateReferencesInManifestWhenThereAreCIDsAndMIDs() throws JsonProcessingException {
        String envelopeText = "\n" +
                "    <soap:Envelope xmlns:eb=\"http://www.oasis-open.org/committees/ebxml-msg/schema/msg-header-2_0.xsd\"\n" +
                "                   xmlns:hl7ebxml=\"urn:hl7-org:transport/ebxml/DSTUv1.0\"\n" +
                "                   xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
                "        <soap:Body>\n" +
                "            <eb:Manifest eb:version=\"2.0\" soap:mustUnderstand=\"1\">\n" +
                "                <eb:Reference xlink:href=\"cid:Content1@e-mis.com/EMISWeb/GP2GP2.2A\"\n" +
                "                              xmlns:xlink=\"http://www.w3.org/1999/xlink\">\n" +
                "                    <eb:Description xml:lang=\"en\">RCMR_IN030000UK06</eb:Description>\n" +
                "                    <hl7ebxml:Payload style=\"HL7\" encoding=\"XML\" version=\"3.0\"/>\n" +
                "                </eb:Reference>\n" +
                "                <eb:Reference xlink:href=\"mid:8F67C905-CF8E-4A02-BCAC-9B1C5A87948F\"\n" +
                "                              eb:id=\"_4AB6EDEE-958F-4828-BDA5-B49285A83B3E\" xmlns:xlink=\"http://www.w3.org/1999/xlink\">\n" +
                "                    <eb:Description xml:lang=\"en\">Filename=\"4AB6EDEE-958F-4828-BDA5-B49285A83B3E_sample.bin\"\n" +
                "                        ContentType=application/octet-stream Compressed=No LargeAttachment=Yes OriginalBase64=No\n" +
                "                        Length=8388608\n" +
                "                    </eb:Description>\n" +
                "                </eb:Reference>\n" +
                "            </eb:Manifest>\n" +
                "        </soap:Body>\n" +
                "    </soap:Envelope>";
        XmlMapper xmlMapper = new XmlMapper();
        SOAPEnvelope envelope = xmlMapper.readValue(envelopeText, SOAPEnvelope.class);
        assertThat(envelope.body.manifest, contains(
                hasProperty("href", equalTo("cid:Content1@e-mis.com/EMISWeb/GP2GP2.2A")),
                hasProperty("href", equalTo("mid:8F67C905-CF8E-4A02-BCAC-9B1C5A87948F"))
        ));
    }

    @Test
    void shouldPopulateReferencesInManifestWhenThereAreOnlyCIDs() throws JsonProcessingException {
        String envelopeText = "<soap:Envelope xmlns:eb=\"http://www.oasis-open.org/committees/ebxml-msg/schema/msg-header-2_0.xsd\"\n" +
                "                       xmlns:hl7ebxml=\"urn:hl7-org:transport/ebxml/DSTUv1.0\"\n" +
                "                       xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
                "            <soap:Body>\n" +
                "                <eb:Manifest eb:version=\"2.0\" soap:mustUnderstand=\"1\">\n" +
                "                    <eb:Reference xlink:href=\"cid:Content1@e-mis.com/EMISWeb/GP2GP2.2A\"\n" +
                "                                  xmlns:xlink=\"http://www.w3.org/1999/xlink\">\n" +
                "                        <eb:Description xml:lang=\"en\">RCMR_IN010000UK05</eb:Description>\n" +
                "                        <hl7ebxml:Payload style=\"HL7\" encoding=\"XML\" version=\"3.0\"/>\n" +
                "                    </eb:Reference>\n" +
                "                </eb:Manifest>\n" +
                "            </soap:Body>\n" +
                "        </soap:Envelope>";
        XmlMapper xmlMapper = new XmlMapper();
        SOAPEnvelope envelope = xmlMapper.readValue(envelopeText, SOAPEnvelope.class);
        assertThat(envelope.body.manifest, contains(
                hasProperty("href", equalTo("cid:Content1@e-mis.com/EMISWeb/GP2GP2.2A"))
        ));
    }

    @Test
    void shouldPopulateReferencesInManifestWhenThereAreMultipleMIDs() throws JsonProcessingException {
        String envelopeText = "<soap:Envelope xmlns:eb=\"http://www.oasis-open.org/committees/ebxml-msg/schema/msg-header-2_0.xsd\"\n" +
                "                   xmlns:hl7ebxml=\"urn:hl7-org:transport/ebxml/DSTUv1.0\"\n" +
                "                   xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
                "        <soap:Body>\n" +
                "            <eb:Manifest eb:version=\"2.0\" soap:mustUnderstand=\"1\">\n" +
                "                <eb:Reference eb:id=\"_4AB6EDEE-958F-4828-BDA5-B49285A83B3E\"\n" +
                "                              xlink:href=\"mid:8F67C905-CF8E-4A02-BCAC-9B1C5A87948F\" xmlns:xlink=\"http://www.w3.org/1999/xlink\">\n" +
                "                    <eb:Description xml:lang=\"en\">Filename=\"4AB6EDEE-958F-4828-BDA5-B49285A83B3E_sample.bin\"\n" +
                "                        ContentType=application/octet-stream Compressed=No LargeAttachment=Yes OriginalBase64=No\n" +
                "                        Length=8388608\n" +
                "                    </eb:Description>\n" +
                "                </eb:Reference>\n" +
                "                <eb:Reference eb:id=\"_4AB6EDEE-958F-4828-BDA5-B49285A83B3E\"\n" +
                "                              xlink:href=\"mid:435B1171-31F6-4EF2-AD7F-C7E64EEFF357\" xmlns:xlink=\"http://www.w3.org/1999/xlink\">\n" +
                "                    <eb:Description xml:lang=\"en\">Filename=\"4AB6EDEE-958F-4828-BDA5-B49285A83B3E_sample.bin\"\n" +
                "                        ContentType=application/octet-stream Compressed=No LargeAttachment=Yes OriginalBase64=No\n" +
                "                        Length=8388608\n" +
                "                    </eb:Description>\n" +
                "                </eb:Reference>\n" +
                "                <eb:Reference xlink:href=\"mid:E39E79A2-FA96-48FF-9373-7BBCB9D036E7\"\n" +
                "                              eb:id=\"_4AB6EDEE-958F-4828-BDA5-B49285A83B3E\" xmlns:xlink=\"http://www.w3.org/1999/xlink\">\n" +
                "                    <eb:Description xml:lang=\"en\">Filename=\"4AB6EDEE-958F-4828-BDA5-B49285A83B3E_sample.bin\"\n" +
                "                        ContentType=application/octet-stream Compressed=No LargeAttachment=Yes OriginalBase64=No\n" +
                "                        Length=8388608\n" +
                "                    </eb:Description>\n" +
                "                </eb:Reference>\n" +
                "            </eb:Manifest>\n" +
                "        </soap:Body>\n" +
                "    </soap:Envelope>";
        XmlMapper xmlMapper = new XmlMapper();
        SOAPEnvelope envelope = xmlMapper.readValue(envelopeText, SOAPEnvelope.class);
        assertThat(envelope.body.manifest, contains(
                hasProperty("href", equalTo("mid:8F67C905-CF8E-4A02-BCAC-9B1C5A87948F")),
                hasProperty("href", equalTo("mid:435B1171-31F6-4EF2-AD7F-C7E64EEFF357")),
                hasProperty("href", equalTo("mid:E39E79A2-FA96-48FF-9373-7BBCB9D036E7"))
        ));
    }
}
