package uk.nhs.prm.repo.suspension.service.repo.in;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.PurgeQueueRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.github.tomakehurst.wiremock.WireMockServer;
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
import uk.nhs.prm.repo.suspension.service.infra.LocalStackAwsConfig;
import uk.nhs.prm.repo.suspension.service.suspensionsevents.SuspensionEventBuilder;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = "toggle.canUpdateManagingOrganisationToRepo=true")
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = LocalStackAwsConfig.class)
@DirtiesContext
public class MOFUpdateToRepoIntegrationTest {

    @Autowired
    private AmazonSQSAsync sqs;

    @Value("${aws.incomingQueueName}")
    private String suspensionsQueueName;

    @Value("${aws.repoIncomingQueueName}")
    private String repoIncomingQueueName;

    private WireMockServer stubPdsAdaptor;

    private String suspensionQueueUrl;

    @BeforeEach
    public void setUp() {
        stubPdsAdaptor = initializeWebServer();
        suspensionQueueUrl = sqs.getQueueUrl(suspensionsQueueName).getQueueUrl();
        purgeQueue(suspensionQueueUrl);
    }

    @AfterEach
    public void tearDown() {
        stubPdsAdaptor.resetAll();
        stubPdsAdaptor.stop();
        suspensionQueueUrl = sqs.getQueueUrl(suspensionsQueueName).getQueueUrl();
        purgeQueue(suspensionQueueUrl);
    }

    private WireMockServer initializeWebServer() {
        final WireMockServer wireMockServer = new WireMockServer(8080);
        wireMockServer.start();
        return wireMockServer;
    }

    @Test
    void shouldSetMOFAsRepoOdsCodeWhenToggleOn(){
        var nhsNumber = Long.toString(System.currentTimeMillis());
        stubFor(get(urlMatching("/suspended-patient-status/" + nhsNumber))
                .withHeader("Authorization", matching("Basic c3VzcGVuc2lvbi1zZXJ2aWNlOiJ0ZXN0Ig=="))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(getSuspendedResponseWith(nhsNumber))));
        stubFor(put(urlMatching("/suspended-patient-status/" + nhsNumber))
                .withHeader("Authorization", matching("Basic c3VzcGVuc2lvbi1zZXJ2aWNlOiJ0ZXN0Ig=="))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(getSuspendedResponseWithRepoOdsCode(nhsNumber))));


        var queueUrl = sqs.getQueueUrl(suspensionsQueueName).getQueueUrl();
        var repoIncomingQueueUrl = sqs.getQueueUrl(repoIncomingQueueName).getQueueUrl();
        purgeQueue(repoIncomingQueueUrl);
        sqs.sendMessage(queueUrl, getSuspensionEventWith(nhsNumber));
        System.out.println("Sent message to queue url " + queueUrl);

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            List<Message> receivedMessageHolder = checkMessageInRelatedQueue(repoIncomingQueueUrl);

            assertTrue(receivedMessageHolder.get(0).getBody().contains("TEST-NEMS-ID"));
            assertTrue(receivedMessageHolder.get(0).getBody().contains("A1234"));
            assertTrue(receivedMessageHolder.get(0).getBody().contains("nemsEventLastUpdated"));
            assertTrue(receivedMessageHolder.get(0).getMessageAttributes().containsKey("traceId"));

        });
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

    private String getSuspensionEventWith(String nhsNumber, String nemsMessageId) {
        return new SuspensionEventBuilder()
                .lastUpdated("2017-11-01T15:00:33+00:00")
                .previousOdsCode("B85612")
                .eventType("SUSPENSION")
                .nhsNumber(nhsNumber)
                .nemsMessageId(nemsMessageId)
                .environment("local").buildJson();
    }

    private String getSuspensionEventWith(String nhsNumber) {
        return getSuspensionEventWith(nhsNumber, "TEST-NEMS-ID");
    }

    private String getSuspendedResponseWith(String nhsNumber) {
        return "{\n" +
                "    \"nhsNumber\": \"" + nhsNumber + "\",\n" +
                "    \"isSuspended\": true,\n" +
                "    \"currentOdsCode\": null,\n" +
                "    \"managingOrganisation\": \"B1234\",\n" +
                "    \"recordETag\": \"W/\\\"5\\\"\",\n" +
                "    \"isDeceased\":  false\n" +
                "}";
    }

    private String getSuspendedResponseWithRepoOdsCode(String nhsNumber) {
        return "{\n" +
                "    \"nhsNumber\": \"" + nhsNumber + "\",\n" +
                "    \"isSuspended\": true,\n" +
                "    \"currentOdsCode\": null,\n" +
                "    \"managingOrganisation\": \"A1234\",\n" +
                "    \"recordETag\": \"W/\\\"5\\\"\",\n" +
                "    \"isDeceased\":  false\n" +
                "}";
    }

    private void purgeQueue(String queueUrl) {
        System.out.println("Purging queue url: " + queueUrl);
        sqs.purgeQueue(new PurgeQueueRequest(queueUrl));
    }
}
