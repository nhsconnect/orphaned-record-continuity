package uk.nhs.prm.repo.suspension.service.publishers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.nhs.prm.repo.suspension.service.model.NonSensitiveDataMessage;

@Component
public class DeceasedPatientEventPublisher {
    private final String deceasedPatientTopicArn;
    private final MessagePublisher messagePublisher;

    public DeceasedPatientEventPublisher(MessagePublisher messagePublisher, @Value("${aws.deceasedPatientSnsTopicArn}") String deceasedPatientSnsTopicAr) {
        this.messagePublisher = messagePublisher;
        this.deceasedPatientTopicArn = deceasedPatientSnsTopicAr;
    }

    public void sendMessage(NonSensitiveDataMessage message) {
        messagePublisher.sendMessage(this.deceasedPatientTopicArn, message.toJsonString());
    }
}

