package uk.nhs.prm.repo.ehrtransferservice.parsers;

import jakarta.jms.JMSException;
import org.apache.activemq.command.ActiveMQBytesMessage;
import org.apache.qpid.proton.amqp.messaging.AmqpValue;
import org.apache.qpid.proton.codec.WritableBuffer;
import org.apache.qpid.proton.message.Message;
import org.apache.qpid.proton.message.ProtonJMessage;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AmqpMessageParserTest {
    private final AmqpMessageParser amqpMessageParser = new AmqpMessageParser();

    private Message createAmqpMessage(String messageBody){
        var message = Message.Factory.create();
        message.setBody(new AmqpValue(messageBody));
        message.setContentType("application/json");
        message.setMessageId(UUID.randomUUID());
        return message;
    }

    private byte[] amqpToBytes(Message message) {
        var amqp = (ProtonJMessage) message;
        var buffer = ByteBuffer.wrap(new byte[1024 * 4]);
        amqp.encode(new WritableBuffer.ByteBufferWrapper(buffer));
        var writableBuffer = new WritableBuffer.ByteBufferWrapper(buffer);
        message.encode(writableBuffer);
        return writableBuffer.toReadableBuffer().array();
    }

    private ActiveMQBytesMessage bytesToBytesMessage(byte[] bytes) throws JMSException {
        ActiveMQBytesMessage bytesMessage = new ActiveMQBytesMessage();
        bytesMessage.writeBytes(bytes);
        bytesMessage.reset();
        return bytesMessage;
    }

    @Test
    void shouldParseMessageBodyFromBytesMessage() throws Exception {
        var messageBody = "A message";
        var amqpMessage = createAmqpMessage(messageBody);
        var bytes = amqpToBytes(amqpMessage);
        var bytesMessage = bytesToBytesMessage(bytes);
        assertEquals(amqpMessageParser.parse(bytesMessage), messageBody);
    }
}
