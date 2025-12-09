package uk.nhs.prm.repo.re_registration.message_publishers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.nhs.prm.repo.re_registration.model.AuditMessage;

@Component
@Slf4j
public class ReRegistrationAuditPublisher {

    private final MessagePublisher messagePublisher;
    private final String reRegistrationAuditTopicArn;

    public ReRegistrationAuditPublisher(MessagePublisher messagePublisher, @Value("${aws.reRegistration.auditTopic.arn}") String reRegistrationAuditTopicArn) {
        this.messagePublisher = messagePublisher;
        this.reRegistrationAuditTopicArn = reRegistrationAuditTopicArn;
    }

    public void sendMessage(AuditMessage message) {
        log.info("Publishing audit message {} to {}" , message.getStatus(), reRegistrationAuditTopicArn);
        messagePublisher.sendMessage(reRegistrationAuditTopicArn, message.toJsonString());
    }
}
