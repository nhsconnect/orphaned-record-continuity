package uk.nhs.prm.repo.re_registration.message_publishers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.repo.re_registration.model.AuditMessage;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReRegistrationAuditPublisherTest {

    @Mock
    private MessagePublisher messagePublisher;

    private final static String reRegistrationTopicArn = "reRegistrationTopicArn";

    private ReRegistrationAuditPublisher reRegistrationAuditPublisher;

    @BeforeEach
    void setUp() {
        reRegistrationAuditPublisher = new ReRegistrationAuditPublisher(messagePublisher, reRegistrationTopicArn);
    }

    @Test
    void shouldSendMessageToRegistrationAuditTopic(){
        reRegistrationAuditPublisher.sendMessage(new AuditMessage("nemsMessageId","status"));
        verify(messagePublisher).sendMessage(reRegistrationTopicArn,"{\"nemsMessageId\":\"nemsMessageId\",\"status\":\"status\"}");
    }
}