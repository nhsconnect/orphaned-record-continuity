package uk.nhs.prm.repo.ehrtransferservice.repo_incoming;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.nhs.prm.repo.ehrtransferservice.activemq.ForceXercesParserExtension;
import uk.nhs.prm.repo.ehrtransferservice.activemq.SimpleAmqpQueue;
import uk.nhs.prm.repo.ehrtransferservice.configuration.LocalStackAwsConfig;
import uk.nhs.prm.repo.ehrtransferservice.database.TransferService;
import uk.nhs.prm.repo.ehrtransferservice.database.enumeration.ConversationTransferStatus;
import uk.nhs.prm.repo.ehrtransferservice.database.model.ConversationRecord;
import uk.nhs.prm.repo.ehrtransferservice.services.ConversationActivityService;
import uk.nhs.prm.repo.ehrtransferservice.utils.SqsQueueUtility;
import uk.nhs.prm.repo.ehrtransferservice.utils.TransferTrackerDbUtility;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.nhs.prm.repo.ehrtransferservice.database.enumeration.ConversationTransferStatus.INBOUND_COMPLETE;
import static uk.nhs.prm.repo.ehrtransferservice.database.enumeration.ConversationTransferStatus.INBOUND_CONTINUE_REQUEST_SENT;
import static uk.nhs.prm.repo.ehrtransferservice.database.enumeration.ConversationTransferStatus.INBOUND_REQUEST_SENT;
import static uk.nhs.prm.repo.ehrtransferservice.database.enumeration.ConversationTransferStatus.INBOUND_TIMEOUT;
import static uk.nhs.prm.repo.ehrtransferservice.database.enumeration.Layer.CONVERSATION;
import static uk.nhs.prm.repo.ehrtransferservice.database.enumeration.TransferTableAttribute.DESTINATION_GP;
import static uk.nhs.prm.repo.ehrtransferservice.utils.MessageParsingUtility.getMessageId;
import static uk.nhs.prm.repo.ehrtransferservice.utils.TestDataLoaderUtility.getTestDataAsString;

@SpringBootTest
@ActiveProfiles("test")
@WireMockTest(httpPort = 8080)
@ExtendWith(SpringExtension.class)
@ExtendWith(MockitoExtension.class)
@ExtendWith(ForceXercesParserExtension.class)
@ContextConfiguration(classes = LocalStackAwsConfig.class)
class EhrRequestTest {
    private static final String NHS_NUMBER = "9798548754";
    private static final UUID INBOUND_CONVERSATION_ID = UUID.fromString("ce3aad10-9b7c-4a9b-ab87-a9d6521d61b2");
    private static final String NEMS_MESSAGE_ID = "eefe01f7-33aa-45ed-8aac-4e0cf68670fd";
    private static final String NEMS_EVENT_LAST_UPDATED = "2017-11-01T15:00:33+00:00";
    private static final String SOURCE_GP = "B14758";
    private static final String PRESIGNED_URL = "http://localhost:8080/presigned/url";
    private static final String REPO_INCOMING_MESSAGE = getRepoIncomingMessage();

    @Autowired
    private TransferService transferService;

    @Autowired
    private TransferTrackerDbUtility transferTrackerDbUtility;

    @Autowired
    private SqsQueueUtility sqsQueueUtility;

    @Autowired
    private ConversationActivityService activityService;

    @Value("${aws.repoIncomingQueueName}")
    private String repoIncomingQueueName;

    @Value("${activemq.inboundQueue}")
    private String inboundQueue;

    @Value("${gp2gpMessengerAuthKey}")
    private String gp2gpMessengerAuthKey;

    @Value("${ehrRepoAuthKey}")
    private String ehrRepoAuthKey;

    @AfterEach
    void afterEach() {
        sqsQueueUtility.purgeQueue(repoIncomingQueueName);
        transferTrackerDbUtility.deleteItem(INBOUND_CONVERSATION_ID, CONVERSATION);
        assertFalse(activityService.isConversationActive(INBOUND_CONVERSATION_ID));
    }

    @Test
    void Given_ValidRepoIncomingEvent_When_PublishedToRepoIncomingQueue_Then_CreateConversationAndUpdateStatusToInboundRequestSentAndCheckInMemoryConversationIsActive() {
        // when
        createStubForGp2GpMessengerEhrRequest();
        sqsQueueUtility.sendSqsMessage(REPO_INCOMING_MESSAGE, repoIncomingQueueName);

        // then
        await().atMost(30, TimeUnit.SECONDS)
                .untilAsserted(() -> assertTrue(conversationStatusMatches(INBOUND_CONVERSATION_ID, INBOUND_REQUEST_SENT)));

        assertTrue(activityService.isConversationActive(INBOUND_CONVERSATION_ID));

        // The following is to stop the activity from interfering with tests run afterwards
        transferService.updateConversationTransferStatus(INBOUND_CONVERSATION_ID, INBOUND_COMPLETE);
    }

    @Test
    void Given_ValidRepoIncomingEventForSmallEhr_When_NoEhrResponseReceived_Then_UpdateStatusToInboundTimeout() {
        // when
        createStubForGp2GpMessengerEhrRequest();
        sqsQueueUtility.sendSqsMessage(REPO_INCOMING_MESSAGE, repoIncomingQueueName);

        // then
        await().atMost(30, TimeUnit.SECONDS)
                .untilAsserted(() -> assertTrue(conversationStatusMatches(INBOUND_CONVERSATION_ID, INBOUND_TIMEOUT)));
    }

    @Test
    void Given_ValidRepoIncomingEventForLargeEhr_When_CoreReceivedButNoFragments_Then_UpdateStatusToInboundTimeout() throws IOException {
        // given
        final SimpleAmqpQueue mhsInboundQueue = new SimpleAmqpQueue(inboundQueue);
        final String largeEhrCore = getTestDataAsString("large-ehr-core");
        final UUID messageId = getMessageId(largeEhrCore).orElseThrow();

        // when
        createStubForGp2GpMessengerEhrRequest();
        stubFetchStorageUrl(INBOUND_CONVERSATION_ID, messageId);
        stubPresignedUrlUploadMessage();
        stubConfirmMessageStored("complete", 201);
        createStubForGp2GpMessengerContinueRequest();

        sqsQueueUtility.sendSqsMessage(REPO_INCOMING_MESSAGE, repoIncomingQueueName);

        await().atMost(30, TimeUnit.SECONDS)
                .untilAsserted(() -> assertTrue(conversationStatusMatches(INBOUND_CONVERSATION_ID, INBOUND_REQUEST_SENT)));

        mhsInboundQueue.sendMessage(largeEhrCore);

        await().atMost(30, TimeUnit.SECONDS)
                .untilAsserted(() -> assertTrue(conversationStatusMatches(INBOUND_CONVERSATION_ID, INBOUND_CONTINUE_REQUEST_SENT)));

        await().atMost(30, TimeUnit.SECONDS)
                .untilAsserted(() -> assertTrue(conversationStatusMatches(INBOUND_CONVERSATION_ID, INBOUND_TIMEOUT)));
    }

    private static String getRepoIncomingMessage() {
        final String repoIncomingEvent = """
            {
                "conversationId": "%s",
                "nhsNumber": "%s",
                "nemsMessageId": "%s",
                "sourceGp": "%s",
                "destinationGp": "%s",
                "nemsEventLastUpdated": "%s"
            }
            """;

        return repoIncomingEvent.formatted(
                INBOUND_CONVERSATION_ID.toString().toUpperCase(),
                NHS_NUMBER,
                NEMS_MESSAGE_ID,
                SOURCE_GP,
                DESTINATION_GP,
                NEMS_EVENT_LAST_UPDATED
        );
    }

    private void createStubForGp2GpMessengerEhrRequest() {
        final String endpoint = "/health-record-requests/%s".formatted(NHS_NUMBER);

        stubFor(post(urlMatching(endpoint))
                .withHeader("Authorization", equalTo(gp2gpMessengerAuthKey))
                .withHeader("Content-Type", equalTo("application/json"))
                .willReturn(aResponse().withStatus(204)));
    }

    private void createStubForGp2GpMessengerContinueRequest() {
        final String endpoint = "/health-record-requests/continue-message";

        stubFor(post(urlMatching(endpoint))
                .withHeader("Authorization", equalTo(gp2gpMessengerAuthKey))
                .withHeader("Content-Type", equalTo("application/json"))
                .willReturn(aResponse().withStatus(204)));
    }

    private void stubFetchStorageUrl(UUID inboundConversationId, UUID messageId) {
        final String endpoint = "/messages/%s/%s".formatted(
                inboundConversationId.toString().toUpperCase(),
                messageId.toString().toUpperCase()
        );

        stubFor(get(urlMatching(endpoint))
                .withHeader("Authorization", equalTo(ehrRepoAuthKey))
                .withHeader("Content-Type", equalTo("application/json"))
                .willReturn(aResponse()
                        // Needs to be a valid URL, parsed into java.net.URL.
                        .withBody(PRESIGNED_URL)
                        .withStatus(200)));
    }

    private void stubPresignedUrlUploadMessage() {
        final String endpoint = "/presigned/url";

        stubFor(put(urlMatching(endpoint))
                .willReturn(aResponse().withStatus(200)));
    }

    private void stubConfirmMessageStored(String healthRecordStatus, int httpResponseStatus) {
        final String endpoint = "/messages";
        final String response = """
                {
                  "healthRecordStatus": "%s"
                }
                """.formatted(healthRecordStatus);

        stubFor(post(urlMatching(endpoint))
                .withHeader("Authorization", equalTo(ehrRepoAuthKey))
                .withHeader("Content-Type", equalTo("application/json"))
                .willReturn(aResponse()
                        .withBody(response)
                        .withStatus(httpResponseStatus)));
    }

    private boolean conversationStatusMatches(UUID inboundConversationId, ConversationTransferStatus conversationTransferStatus) {
        try {
            return transferService.getConversationTransferStatus(inboundConversationId).equals(conversationTransferStatus);
        } catch (Exception exception) { return false; }
    }
}