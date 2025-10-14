package uk.nhs.prm.repo.suspension.service.infra;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;
import com.amazonaws.services.sqs.model.GetQueueAttributesResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import software.amazon.awssdk.services.dynamodb.waiters.DynamoDbWaiter;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.CreateTopicRequest;
import software.amazon.awssdk.services.sns.model.CreateTopicResponse;
import software.amazon.awssdk.services.sns.model.SubscribeRequest;

import jakarta.annotation.PostConstruct;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@TestConfiguration
public class LocalStackAwsConfig {

    @Autowired
    private AmazonSQSAsync amazonSQSAsync;

    @Autowired
    private SnsClient snsClient;

    @Autowired
    private DynamoDbClient dynamoDbClient;

    @Value("${aws.incomingQueueName}")
    private String suspensionsQueueName;

    @Value("${aws.notSuspendedQueueName}")
    private String notSuspendedQueueName;

    @Value("${aws.mofUpdatedQueueName}")
    private String mofUpdatedQueueName;

    @Value("${aws.eventOutOfOrderQueueName}")
    private String eventOutOfOrderQueueName;

    @Value("${aws.invalidSuspensionAuditQueueName}")
    private String invalidSuspensionAuditQueueName;

    @Value("${aws.invalidSuspensionQueueName}")
    private String invalidSuspensionQueueName;

    @Value("${aws.suspensionDynamoDbTableName}")
    private String suspensionDynamoDbTableName;

    @Value("${aws.acknowledgementQueue}")
    private String ackQueueName;

    @Value("${aws.repoIncomingQueueName}")
    private String repoIncomingQueueName;

    @Value("${aws.activeSuspensionsQueueName}")
    private String activeSuspensionsQueueName;

    @Bean
    public static AmazonSQSAsync amazonSQSAsync(@Value("${localstack.url}") String localstackUrl) {
        return AmazonSQSAsyncClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials("LSIA5678901234567890", "LSIA5678901234567890")))
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(localstackUrl, "eu-west-2"))
                .build();
    }

    @Bean
    public static SnsClient snsClient(@Value("${localstack.url}") String localstackUrl) {
        return SnsClient.builder()
                .endpointOverride(URI.create(localstackUrl))
                .region(Region.EU_WEST_2)
                .credentialsProvider(StaticCredentialsProvider.create(new AwsCredentials() {
                    @Override
                    public String accessKeyId() {
                        return "LSIA5678901234567890";
                    }

                    @Override
                    public String secretAccessKey() {
                        return "LSIA5678901234567890";
                    }
                }))
                .build();
    }

    @Bean
    public static DynamoDbClient dynamoDbClient(@Value("${localstack.url}") String localstackUrl) {
        return DynamoDbClient.builder()
                .endpointOverride(URI.create(localstackUrl))
                .region(Region.EU_WEST_2)
                .credentialsProvider(
                        StaticCredentialsProvider.create(new AwsCredentials() {
                            @Override
                            public String accessKeyId() {
                                return "LSIA5678901234567890";
                            }

                            @Override
                            public String secretAccessKey() {
                                return "LSIA5678901234567890";
                            }
                        }))
                .build();
    }

    @Bean
    @Primary
    public static CloudWatchClient cloudwatchClient(@Value("${localstack.url}") String localstackUrl) {
        return CloudWatchClient.builder()
                .endpointOverride(URI.create(localstackUrl))
                .region(Region.EU_WEST_2)
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("LSIAQAAAAAAVNCBMPNSG", "LSIAQAAAAAAVNCBMPNSG")))
                .build();
    }

    @PostConstruct
    public void setupTestQueuesAndTopics() {
        amazonSQSAsync.createQueue(suspensionsQueueName);
        amazonSQSAsync.createQueue(ackQueueName);
        var notSuspendedQueue = amazonSQSAsync.createQueue(notSuspendedQueueName);
        var mofUpdatedQueue = amazonSQSAsync.createQueue(mofUpdatedQueueName);
        var eventOutOfOrderQueue = amazonSQSAsync.createQueue(eventOutOfOrderQueueName);
        var invalidSuspensionQueue = amazonSQSAsync.createQueue(invalidSuspensionQueueName);
        var invalidSuspensionAuditQueue = amazonSQSAsync.createQueue(invalidSuspensionAuditQueueName);
        var incomingQueue = amazonSQSAsync.createQueue(repoIncomingQueueName);
        //Queue created in re-registration service, only used here for checking that message is received on active-suspensions topic
        var activeSuspensionsQueue = amazonSQSAsync.createQueue(activeSuspensionsQueueName);

        var topic = snsClient.createTopic(CreateTopicRequest.builder().name("test_not_suspended_topic").build());
        var mofUpdatedTopic = snsClient.createTopic(CreateTopicRequest.builder().name("mof_updated_sns_topic").build());
        var eventOutOfOrderTopic = snsClient.createTopic(CreateTopicRequest.builder().name("event_out_of_order_topic").build());
        var invalidSuspensionTopic = snsClient.createTopic(CreateTopicRequest.builder().name("invalid_suspension_topic").build());
        var nonSensitiveInvalidSuspensionTopic = snsClient.createTopic(CreateTopicRequest.builder().name("invalid_suspension_audit_topic").build());
        var repoIncomingTopic = snsClient.createTopic(CreateTopicRequest.builder().name("repo_incoming_sns_topic").build());
        var activeSuspensionsTopic = snsClient.createTopic(CreateTopicRequest.builder().name("active_suspensions_sns_topic").build());

        createSnsTestReceiverSubscription(topic, getQueueArn(notSuspendedQueue.getQueueUrl()));
        createSnsTestReceiverSubscription(mofUpdatedTopic, getQueueArn(mofUpdatedQueue.getQueueUrl()));
        createSnsTestReceiverSubscription(eventOutOfOrderTopic, getQueueArn(eventOutOfOrderQueue.getQueueUrl()));
        createSnsTestReceiverSubscription(invalidSuspensionTopic, getQueueArn(invalidSuspensionQueue.getQueueUrl()));
        createSnsTestReceiverSubscription(nonSensitiveInvalidSuspensionTopic, getQueueArn(invalidSuspensionAuditQueue.getQueueUrl()));
        createSnsTestReceiverSubscription(repoIncomingTopic, getQueueArn(incomingQueue.getQueueUrl()));
        createSnsTestReceiverSubscription(activeSuspensionsTopic, getQueueArn(activeSuspensionsQueue.getQueueUrl()));

        setupDbAndTable();
    }

    private void setupDbAndTable() {

        var waiter = dynamoDbClient.waiter();
        var tableRequest = DescribeTableRequest.builder()
                .tableName(suspensionDynamoDbTableName)
                .build();

        if (dynamoDbClient.listTables().tableNames().contains(suspensionDynamoDbTableName)) {
            resetTableForLocalEnvironment(waiter, tableRequest);
        }


        List<KeySchemaElement> keySchema = new ArrayList<>();
        keySchema.add(KeySchemaElement.builder()
                .keyType(KeyType.HASH)
                .attributeName("nhs_number")
                .build());

        var attributeDefinitions = new ArrayList<AttributeDefinition>();
        attributeDefinitions.add(AttributeDefinition.builder()
                .attributeType(ScalarAttributeType.S)
                .attributeName("nhs_number")
                .build());

        var createTableRequest = CreateTableRequest.builder()
                .tableName(suspensionDynamoDbTableName)
                .keySchema(keySchema)
                .attributeDefinitions(attributeDefinitions)
                .provisionedThroughput(ProvisionedThroughput.builder()
                        .readCapacityUnits(5L)
                        .writeCapacityUnits(5L)
                        .build())
                .build();

        dynamoDbClient.createTable(createTableRequest);
        waiter.waitUntilTableExists(tableRequest);
    }

    private void resetTableForLocalEnvironment(DynamoDbWaiter waiter, DescribeTableRequest tableRequest) {
        var deleteRequest = DeleteTableRequest.builder().tableName(suspensionDynamoDbTableName).build();
        dynamoDbClient.deleteTable(deleteRequest);
        waiter.waitUntilTableNotExists(tableRequest);
    }

    private void createSnsTestReceiverSubscription(CreateTopicResponse topic, String queueArn) {
        final Map<String, String> attributes = new HashMap<>();
        attributes.put("RawMessageDelivery", "True");
        SubscribeRequest subscribeRequest = SubscribeRequest.builder()
                .topicArn(topic.topicArn())
                .protocol("sqs")
                .endpoint(queueArn)
                .attributes(attributes)
                .build();

        snsClient.subscribe(subscribeRequest);
    }

    private String getQueueArn(String queueUrl) {
        GetQueueAttributesResult queueAttributes = amazonSQSAsync.getQueueAttributes(queueUrl, List.of("QueueArn"));
        return queueAttributes.getAttributes().get("QueueArn");
    }
}
