package uk.nhs.prm.repo.ehrtransferservice;

import jakarta.jms.JMSException;
import jakarta.jms.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.stereotype.Component;
import uk.nhs.prm.repo.ehrtransferservice.logging.Tracer;
import uk.nhs.prm.repo.ehrtransferservice.message_publishers.ParsingDlqPublisher;
import uk.nhs.prm.repo.ehrtransferservice.parsers.AmqpMessageParser;
import uk.nhs.prm.repo.ehrtransferservice.parsers.Parser;
import uk.nhs.prm.repo.ehrtransferservice.services.Broker;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class JmsConsumer {
    final AmqpMessageParser amqpMessageParser;
    final Parser parser;
    private final Broker broker;
    private final Tracer tracer;
    private final ParsingDlqPublisher parsingDlqPublisher;
    private static final String UNPROCESSABLE_MESSAGE_BODY = "NO_ACTION:UNPROCESSABLE_MESSAGE_BODY";

    @JmsListener(destination = "${activemq.inboundQueue}")
    public void onMessage(Message message,
                          @Headers Map<String, Object> headers) {
        String messageBody = null;

        try {
            var traceContext = tracer.createNewContext();

            traceContext.updateTraceId(getCorrelationId(headers));
            debugMessageFormatInfo(message, headers);

            messageBody = amqpMessageParser.parse(message);
            log.info("Received Amqp Message from Inbound queue");
            var parsedMessage = parser.parse(messageBody);

            traceContext.updateConversationId(parsedMessage.getConversationId().toString().toUpperCase());
            log.info("Successfully parsed message");

            if (parsedMessage.getInteractionId() == null || parsedMessage.getInteractionId().isBlank()) {
                log.warn("Sending message without Interaction ID to dlq");
                parsingDlqPublisher.sendMessage(messageBody);
                return;
            }

            broker.sendMessageToEhrInOrUnhandled(parsedMessage);
        } catch (Exception e) {
            var toBeSentToDlq = messageBody != null ? messageBody : UNPROCESSABLE_MESSAGE_BODY;
            log.error("Failed to process message - sending to dlq");
            parsingDlqPublisher.sendMessage(toBeSentToDlq);
        }
    }

    private String getCorrelationId(Map<String, Object> headers) {
        if (headers.containsKey("correlation-id")) {
            return headers.get("correlation-id").toString();
        }
        return null;
    }

    private void debugMessageFormatInfo(Message message, Map<String, Object> headers) throws JMSException {
        log.debug("Class of inbound message: " + message.getClass().getName());
        log.debug("JMS correlation id: " + message.getJMSCorrelationID());

        log.debug("property names: " + Collections.list(message.getPropertyNames()).stream().collect(Collectors.joining(", ")));

        log.debug("correlation-id property: " + message.getStringProperty("correlation-id"));
        log.debug("JMS_AMQP_NATIVE property: " + message.getStringProperty("JMS_AMQP_NATIVE"));
        log.debug("message-format property: " + message.getStringProperty("message-format"));
        log.debug("JMS_AMQP_ContentType property: " + message.getStringProperty("JMS_AMQP_ContentType"));
        log.debug("JMS_AMQP_HEADER property: " + message.getStringProperty("JMS_AMQP_HEADER"));

        log.debug("headers: " + headers
                .entrySet()
                .stream()
                .map(kv -> kv.getKey() + ":" + kv.getValue())
                .collect(Collectors.joining(", "))
        );

        log.debug("correlation-id header: " + headers.get("correlation-id"));
        log.debug("JMS_AMQP_NATIVE header: " + headers.get("JMS_AMQP_NATIVE"));
        log.debug("message-format header: " + headers.get("message-format"));
    }
}