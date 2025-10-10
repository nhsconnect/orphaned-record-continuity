package uk.nhs.prm.deductions.nemseventprocessor.nemsevents;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.CreateTopicRequest;
import software.amazon.awssdk.services.sns.model.CreateTopicResponse;
import software.amazon.awssdk.services.sns.model.SubscribeRequest;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@TestConfiguration
public class LocalStackAwsConfig {

    public final static String UNHANDLED_EVENTS_TEST_RECEIVING_QUEUE = "unhandled_test_receiver";
    public final static String SUSPENSIONS_TEST_RECEIVING_QUEUE = "suspensions_test_receiver";
    public final static String RE_REGISTRATION_TEST_RECEIVING_QUEUE = "re_registration_test_receiver";
    public final static String NEMS_EVENTS_AUDIT_TEST_RECEIVING_QUEUE = "nems_events_audit_test_receiver";

    @Autowired
    private SqsClient sqsClient;

    @Autowired
    private SnsClient snsClient;

    @Value("${aws.nemsEventsQueueName}")
    private String nemsEventQueueName;

    @Bean
    @Primary
    public static SqsClient sqsClient(@Value("${localstack.url}") String localstackUrl) {
        System.out.println("localstack config sqsClient creation");
        return SqsClient.builder()
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("LSIAQAAAAAAVNCBMPNSG", "LSIAQAAAAAAVNCBMPNSG")))
                .region(Region.EU_WEST_2)
                .endpointOverride(URI.create(localstackUrl))
                .build();
    }

    @Bean
    @Primary
    public static SnsClient snsClient(@Value("${localstack.url}") String localstackUrl) {
        return SnsClient.builder()
                .endpointOverride(URI.create(localstackUrl))
                .region(Region.EU_WEST_2)
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("LSIAQAAAAAAVNCBMPNSG", "LSIAQAAAAAAVNCBMPNSG")))
                .build();
    }

    @PostConstruct
    public void setupTestQueuesAndTopics() {
        recreateIncomingNemsQueue();
        CreateTopicResponse topic = snsClient.createTopic(CreateTopicRequest.builder().name("test_unhandled_events_topic").build());
        CreateTopicResponse suspensionsTopic = snsClient.createTopic(CreateTopicRequest.builder().name("test_suspensions_topic").build());
        CreateTopicResponse reRegistrationTopic = snsClient.createTopic(CreateTopicRequest.builder().name("test_re_registration_topic").build());
        CreateTopicResponse nemsEventsAuditTopic =
            snsClient.createTopic(CreateTopicRequest.builder().name("test_nems_events_audit_topic").build());
        snsClient.createTopic(CreateTopicRequest.builder().name("test_dead_letter_topic").build());

        createSnsTestReceiverSubscription(topic, UNHANDLED_EVENTS_TEST_RECEIVING_QUEUE);
        createSnsTestReceiverSubscription(suspensionsTopic, SUSPENSIONS_TEST_RECEIVING_QUEUE);
        createSnsTestReceiverSubscription(reRegistrationTopic, RE_REGISTRATION_TEST_RECEIVING_QUEUE);
        createSnsTestReceiverSubscription(nemsEventsAuditTopic, NEMS_EVENTS_AUDIT_TEST_RECEIVING_QUEUE);
    }

    private void recreateIncomingNemsQueue() {
        ensureQueueDeleted(nemsEventQueueName);
        createQueue(nemsEventQueueName);
    }

    private CreateQueueResponse createQueue(String queueName) {
        return sqsClient.createQueue(CreateQueueRequest.builder().queueName(queueName).build());
    }

    private void ensureQueueDeleted(String queueName) {
        try {
            deleteQueue(queueName);
        }
        catch (QueueDoesNotExistException e) {
            // no biggie
        }
    }

    private void deleteQueue(String queueName) {
        GetQueueUrlRequest getQueueRequest = GetQueueUrlRequest.builder()
                .queueName(queueName)
                .build();

        String queueUrl = sqsClient.getQueueUrl(getQueueRequest).queueUrl();

        sqsClient.deleteQueue(DeleteQueueRequest.builder().queueUrl(queueUrl).build());
    }

    private void createSnsTestReceiverSubscription(CreateTopicResponse topic, String queue) {
        String testReceiverQueueArn = createQueueAndGetArn(queue);
        final Map<String, String> attributes = new HashMap<>();
        attributes.put("RawMessageDelivery", "True");
        SubscribeRequest subscribeRequest = SubscribeRequest.builder()
                .topicArn(topic.topicArn())
                .protocol("sqs")
                .endpoint(testReceiverQueueArn)
                .attributes(attributes)
                .build();

        snsClient.subscribe(subscribeRequest);
    }

    private String createQueueAndGetArn(String queueName) {
        var testReceiver = createQueue(queueName);
        var attributesRequest = GetQueueAttributesRequest.builder()
                .queueUrl(testReceiver.queueUrl())
                .attributeNames(QueueAttributeName.QUEUE_ARN).build();
        var queueAttributes = sqsClient.getQueueAttributes(attributesRequest);
        return queueAttributes.attributes().get(QueueAttributeName.QUEUE_ARN);
    }
}

