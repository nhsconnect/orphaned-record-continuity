package uk.nhs.prm.repo.re_registration.message_publishers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.MessageAttributeValue;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import uk.nhs.prm.repo.re_registration.config.Tracer;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class MessagePublisher {
    private final SnsClient snsClient;
    private final Tracer tracer;

    public void sendMessage(String topicArn, String message) {
        sendMessage(topicArn, message, null, null);
    }

    public void sendMessage(String topicArn, String message, String attributeKey, String attributeValue) {
        var messageAttributes = createMessageAttributes();
        if (attributeKey != null) {
            messageAttributes.put(attributeKey, getMessageAttributeValue(attributeValue));
        }

        PublishRequest request = PublishRequest.builder()
                .message(message)
                .messageAttributes(messageAttributes)
                .topicArn(topicArn)
                .build();

        var result = snsClient.publish(request);
        var topicAttributes = topicArn.split(":");
        log.info("PUBLISHED: message to {} topic. Published SNS message id: {}", topicAttributes[topicAttributes.length-1], result.messageId());
    }

    private Map<String, MessageAttributeValue> createMessageAttributes() {
        Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
        messageAttributes.put("traceId", getMessageAttributeValue(tracer.getTraceId()));
        return messageAttributes;
    }

    private MessageAttributeValue getMessageAttributeValue(String attributeValue) {
        return MessageAttributeValue.builder()
                .dataType("String")
                .stringValue(attributeValue)
                .build();
    }
}
