package uk.nhs.prm.repo.suspension.service.publishers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.nhs.prm.repo.suspension.service.config.Tracer;
import uk.nhs.prm.repo.suspension.service.model.NonSensitiveDataMessage;

@Component
public class InvalidSuspensionPublisher {
    private final String invalidSuspensionSensitiveTopicArn;
    private final String invalidSuspensionAuditTopicArn;
    private final MessagePublisher messagePublisher;
    private final Tracer tracer;

    public InvalidSuspensionPublisher(MessagePublisher messagePublisher, @Value("${aws.invalidSuspensionSnsTopicArn}") String invalidSuspensionSensitiveTopicArn,
                                      @Value("${aws.invalidSuspensionAuditSnsTopicArn}") String invalidSuspensionAuditTopicArn, Tracer tracer) {
        this.messagePublisher = messagePublisher;
        this.invalidSuspensionSensitiveTopicArn = invalidSuspensionSensitiveTopicArn;
        this.invalidSuspensionAuditTopicArn = invalidSuspensionAuditTopicArn;
        this.tracer = tracer;
    }

    public void sendInvalidMessageAndAuditMessage(String sensitiveMessage, String nemsMessageId) {
        sendSensitiveMessage(sensitiveMessage);
        sendInvalidAuditMessage(nemsMessageId);
    }

    private void sendSensitiveMessage(String message) {
        messagePublisher.sendMessage(invalidSuspensionSensitiveTopicArn, message);
    }

    private void sendInvalidAuditMessage(String nemsMessageId) {
        var verifiedNemsMessageId = verifyNemsMessageId(nemsMessageId);
        if (verifiedNemsMessageId != null) {
            var invalidSuspensionMessage = new NonSensitiveDataMessage(verifiedNemsMessageId, "NO_ACTION:INVALID_SUSPENSION");
            messagePublisher.sendMessage(invalidSuspensionAuditTopicArn, invalidSuspensionMessage.toJsonString());
        }
    }

    private String verifyNemsMessageId(String nemsMessageId) {
        return nemsMessageId == null ? tracer.getNemsMessageId() : nemsMessageId;
    }
}

