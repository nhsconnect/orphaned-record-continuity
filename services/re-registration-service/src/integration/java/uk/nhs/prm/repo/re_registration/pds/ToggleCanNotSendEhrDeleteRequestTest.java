package uk.nhs.prm.repo.re_registration.pds;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.PurgeQueueRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.nhs.prm.repo.re_registration.data.ActiveSuspensionsDb;
import uk.nhs.prm.repo.re_registration.infra.LocalStackAwsConfig;
import uk.nhs.prm.repo.re_registration.model.ActiveSuspensionsMessage;
import uk.nhs.prm.repo.re_registration.model.ReRegistrationEvent;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(properties = "toggle.canSendDeleteEhrRequest=false")
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@ContextConfiguration( classes = LocalStackAwsConfig.class)
@DirtiesContext
public class ToggleCanNotSendEhrDeleteRequestTest {

    public static final String NHS_NUMBER = "1234567890";
    public static final String STATUS_FOR_RECEIVED_REGISTRATION_EVENT = "NO_ACTION:RE_REGISTRATION_EVENT_RECEIVED";

    @Autowired
    private AmazonSQSAsync sqs;

    @Autowired
    private ActiveSuspensionsDb activeSuspensionsDb;

    @Value("${aws.reRegistrationsQueueName}")
    private String reRegistrationsQueueName;

    @Value("${aws.reRegistrationsAuditQueueName}")
    private String reRegistrationsAuditQueueName;

    private String reRegistrationsQueueUrl;
    private String reRegistrationsAuditUrl;
    private String nemsMessageId = "nemsMessageId";


    @BeforeEach
    public void setUp() {
        reRegistrationsQueueUrl = sqs.getQueueUrl(reRegistrationsQueueName).getQueueUrl();
        reRegistrationsAuditUrl = sqs.getQueueUrl(reRegistrationsAuditQueueName).getQueueUrl();
    }

    @AfterEach
    public void tearDown() {
        sqs.purgeQueue(new PurgeQueueRequest(reRegistrationsAuditUrl));
    }

    @Test
    void shouldSendToAuditQueueAndNotProcessMessageWhenToggleIsFalseAndActiveSuspensionIsFound() {
        var activeSuspensionsMessage = new ActiveSuspensionsMessage(NHS_NUMBER, "previous-ods-code", "2017-11-01T15:00:33+00:00");
        activeSuspensionsDb.save(activeSuspensionsMessage);
        sqs.sendMessage(reRegistrationsQueueUrl,getReRegistrationEvent().toJsonString());

        await().atMost(20, TimeUnit.SECONDS).untilAsserted(()-> {
            String messageBody = checkMessageInRelatedQueue(reRegistrationsAuditUrl).get(0).getBody();
            assertThat(messageBody).contains(STATUS_FOR_RECEIVED_REGISTRATION_EVENT);
            assertThat(messageBody).contains(nemsMessageId);
        });
    }

    @Test
    void shouldSendUnknownMessageToAuditQueueAndNotProcessMessageWhenToggleIsFalseAndActiveSuspensionNotIsFound() {
        sqs.sendMessage(reRegistrationsQueueUrl,getReRegistrationEvent().toJsonString());

        await().atMost(20, TimeUnit.SECONDS).untilAsserted(()-> {
            String messageBody = checkMessageInRelatedQueue(reRegistrationsAuditUrl).get(0).getBody();
            assertThat(messageBody).contains("NO_ACTION:UNKNOWN_REGISTRATION_EVENT_RECEIVED");
        });
    }

    private ReRegistrationEvent getReRegistrationEvent() {
        return new ReRegistrationEvent(NHS_NUMBER, "ABC123", nemsMessageId, "2017-11-01T15:00:33+00:00");
    }

    private List<Message> checkMessageInRelatedQueue(String queueUrl) {
        System.out.println("checking sqs queue: " + queueUrl);

        var requestForMessagesWithAttributes
                = new ReceiveMessageRequest().withQueueUrl(queueUrl)
                .withMessageAttributeNames("traceId");
        List<Message> messages = sqs.receiveMessage(requestForMessagesWithAttributes).getMessages();
        System.out.printf("Found %s messages on queue: %s%n", messages.size(), queueUrl);
        assertThat(messages).hasSize(1);
        return messages;
    }

}
