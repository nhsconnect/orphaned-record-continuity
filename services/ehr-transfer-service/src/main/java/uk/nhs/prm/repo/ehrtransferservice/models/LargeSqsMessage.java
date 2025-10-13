package uk.nhs.prm.repo.ehrtransferservice.models;

import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.MessageContent;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.ParsedMessage;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.SOAPEnvelope;

public class LargeSqsMessage extends ParsedMessage {
    public LargeSqsMessage(SOAPEnvelope soapEnvelope, MessageContent messageContent, String messageBody) {
        super(soapEnvelope, messageContent, messageBody);
    }
}