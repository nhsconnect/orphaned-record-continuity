package uk.nhs.prm.repo.ehrtransferservice.parsers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.EhrExtractMessageWrapper;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.MhsJsonMessage;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.SOAPEnvelope;
import uk.nhs.prm.repo.ehrtransferservice.models.LargeSqsMessage;

@Slf4j
@Component
@RequiredArgsConstructor
public class LargeSqsMessageParser {

    //TODO: same logic duplicated from Parser.parse. Room for improvement
    public LargeSqsMessage parse(String messageBody) throws JsonProcessingException {
        XmlMapper xmlMapper = new XmlMapper();
        var mhsJsonMessage = new ObjectMapper().readValue(messageBody, MhsJsonMessage.class);
        var envelope = xmlMapper.readValue(mhsJsonMessage.ebXML, SOAPEnvelope.class);
        var message = xmlMapper.readValue(mhsJsonMessage.payload, EhrExtractMessageWrapper.class);
        return new LargeSqsMessage(envelope, message, messageBody);
    }
}
