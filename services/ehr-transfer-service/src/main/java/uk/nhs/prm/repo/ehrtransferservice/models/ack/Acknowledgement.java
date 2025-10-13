package uk.nhs.prm.repo.ehrtransferservice.models.ack;

import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.AcknowledgementMessageWrapper;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.MessageContent;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.ParsedMessage;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.SOAPEnvelope;

import java.util.List;

public class Acknowledgement extends ParsedMessage {
    public Acknowledgement(SOAPEnvelope soapEnvelope, MessageContent messageContent, String messageBody) {
        super(soapEnvelope, messageContent, messageBody);
    }

    public List<FailureDetail> getFailureDetails() {
        return acknowledgementContent().getFailureDetails();
    }

    public AcknowledgementTypeCode getTypeCode() {
        return acknowledgementContent().getTypeCode();
    }

    private AcknowledgementMessageWrapper acknowledgementContent() {
        return (AcknowledgementMessageWrapper) getMessageContent();
    }

    public boolean isNegativeAcknowledgement() {
        return getTypeCode() != AcknowledgementTypeCode.AA;
    }
}
