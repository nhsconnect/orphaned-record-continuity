package uk.nhs.prm.deductions.nemseventprocessor.audit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.nhs.prm.deductions.nemseventprocessor.MessagePublisher;

@Component
@Slf4j
public class AuditEventPublisher {

    private final String nemsEventsAuditTopicArn;
    private final MessagePublisher messagePublisher;

    public AuditEventPublisher(MessagePublisher messagePublisher, @Value("${aws.nemsEventsAuditTopicArn}") String nemsEventsAuditTopicArn) {
        this.messagePublisher = messagePublisher;
        this.nemsEventsAuditTopicArn = nemsEventsAuditTopicArn;
    }

    public void sendMessage(AuditMessage auditMessage) {
        log.info("Publishing nems event to audit");
        messagePublisher.sendMessage(nemsEventsAuditTopicArn, auditMessage.toJsonString());
    }
}
