package uk.nhs.prm.repo.re_registration.infra;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.GetQueueAttributesResult;
import com.amazonaws.services.sqs.model.QueueDoesNotExistException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DeleteTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.ProvisionedThroughput;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;
import software.amazon.awssdk.services.dynamodb.waiters.DynamoDbWaiter;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.CreateTopicRequest;
import software.amazon.awssdk.services.sns.model.CreateTopicResponse;
import software.amazon.awssdk.services.sns.model.SubscribeRequest;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@TestConfiguration
public class LocalStackAwsConfig {

    @Value("${aws.reRegistrationsQueueName}")
    private String reRegistrationsQueueName;

    @Value("${aws.activeSuspensionsQueueName}")
    private String activeSuspensionsQueueName;

    @Autowired
    private SnsClient snsClient;

    @Value("${aws.reRegistrationsAuditQueueName}")
    private String reRegistrationsAuditQueueName;

    @Value("${aws.region}")
    private String awsRegion;

    @Autowired
    private AmazonSQSAsync amazonSQSAsync;

    @Autowired
    private DynamoDbClient dynamoDbClient;

    @Value("${aws.activeSuspensionsDynamoDbTableName}")
    private String activeSuspensionsDynamoDbTableName;

    @Bean
    public static SqsClient sqsClient(@Value("${localstack.url}") String localstackUrl) throws URISyntaxException {
        return SqsClient.builder()
                .credentialsProvider((() -> AwsBasicCredentials.create("FAKE", "FAKE")))
                .endpointOverride(new URI(localstackUrl))
                .build();
    }

    @Bean
    public static AmazonSQSAsync amazonSQSAsync(@Value("${localstack.url}") String localstackUrl) {
        return AmazonSQSAsyncClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials("FAKE", "FAKE")))
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
                        return "FAKE";
                    }

                    @Override
                    public String secretAccessKey() {
                        return "FAKE";
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
                                return "FAKE";
                            }

                            @Override
                            public String secretAccessKey() {
                                return "FAKE";
                            }
                        }))
                .build();
    }

    @PostConstruct
    public void setupTestQueuesTopicsAndDb() {
        recreateReRegistrationsQueue();
        var reRegistrationsAuditQueue = amazonSQSAsync.createQueue(reRegistrationsAuditQueueName);
        var topic = snsClient.createTopic(CreateTopicRequest.builder().name("re_registration_audit_sns_topic").build());

        createSnsTestReceiverSubscription(topic, getQueueArn(reRegistrationsAuditQueue.getQueueUrl()));

        setupDbAndTable();
    }

    private void setupDbAndTable() {

        var waiter = dynamoDbClient.waiter();
        var tableRequest = DescribeTableRequest.builder()
                .tableName(activeSuspensionsDynamoDbTableName)
                .build();

        if (dynamoDbClient.listTables().tableNames().contains(activeSuspensionsDynamoDbTableName)) {
            resetTableForLocalEnvironment(waiter, tableRequest);
        }


        List<KeySchemaElement> keySchema = new ArrayList<>();
        keySchema.add(KeySchemaElement.builder()
                .keyType(KeyType.HASH)
                .attributeName("nhs_number")
                .build());

        List<AttributeDefinition> attributeDefinitions = new ArrayList<AttributeDefinition>();
        attributeDefinitions.add(AttributeDefinition.builder()
                .attributeType(ScalarAttributeType.S)
                .attributeName("nhs_number")
                .build());

        var createTableRequest = CreateTableRequest.builder()
                .tableName(activeSuspensionsDynamoDbTableName)
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
        var deleteRequest = DeleteTableRequest.builder().tableName(activeSuspensionsDynamoDbTableName).build();
        dynamoDbClient.deleteTable(deleteRequest);
        waiter.waitUntilTableNotExists(tableRequest);
    }

    private void recreateReRegistrationsQueue() {
        ensureQueueDeleted(reRegistrationsQueueName);
        ensureQueueDeleted(activeSuspensionsQueueName);
        createQueue(reRegistrationsQueueName);
        createQueue(activeSuspensionsQueueName);
    }

    private void createQueue(String queueName) {
        CreateQueueRequest createQueueRequest = new CreateQueueRequest();
        createQueueRequest.setQueueName(queueName);
        HashMap<String, String> attributes = new HashMap<>();
        createQueueRequest.withAttributes(attributes);
        amazonSQSAsync.createQueue(queueName);
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

    private void ensureQueueDeleted(String queueName) {
        try {
            deleteQueue(queueName);
        } catch (QueueDoesNotExistException e) {
            // no biggie
        }
    }

    private void deleteQueue(String queueName) {
        amazonSQSAsync.deleteQueue(amazonSQSAsync.getQueueUrl(queueName).getQueueUrl());
    }

    private String getQueueArn(String queueUrl) {
        GetQueueAttributesResult queueAttributes = amazonSQSAsync.getQueueAttributes(queueUrl, List.of("QueueArn"));
        return queueAttributes.getAttributes().get("QueueArn");
    }

}
