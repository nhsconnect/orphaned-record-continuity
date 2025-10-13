package uk.nhs.prm.repo.ehrtransferservice.parsers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.*;
import uk.nhs.prm.repo.ehrtransferservice.models.ack.Acknowledgement;

import java.io.IOException;

@Component
@Slf4j
public class Parser {
    private final XmlMapper xmlMapper = new XmlMapper();

    public ParsedMessage parse(String messageBodyAsString) throws IOException {
        var mhsJsonMessage = new ObjectMapper().readValue(messageBodyAsString, MhsJsonMessage.class);
        var envelope = xmlMapper.readValue(mhsJsonMessage.ebXML, SOAPEnvelope.class);
        MessageContent message = null;
        switch (envelope.header.messageHeader.action) {
            case "RCMR_IN030000UK06":
                message = xmlMapper.readValue(mhsJsonMessage.payload, EhrExtractMessageWrapper.class);
            break;
            case "RCMR_IN010000UK05":
                message = xmlMapper.readValue(mhsJsonMessage.payload, EhrRequestMessageWrapper.class);
                break;
            case "MCCI_IN010000UK13":
                message = xmlMapper.readValue(mhsJsonMessage.payload, AcknowledgementMessageWrapper.class);
                return new Acknowledgement(envelope, message, messageBodyAsString);
            case "COPC_IN000001UK01":
                log.info("COPC message received in Parser");
                break;
            default:
                log.warn("No interaction ID match found for current message");
                break;
        }
        return new ParsedMessage(envelope, message, messageBodyAsString);
    }
}
