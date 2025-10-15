package uk.nhs.prm.repo.suspension.service.metrics;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;

@Configuration
public class AppConfig {

    private final String environment;
    private final String metricNamespace;
    private final String suspensionsQueueName;
    private final String notSuspendedSnsTopicArn;
    private final String notSuspendedQueueName;
    private final String suspensionDynamoDbTableName;

    public AppConfig(@Value("${environment}") String environment,
                     @Value("${metricNamespace}") String metricNamespace,
                     @Value("${aws.incomingQueueName}") String suspensionsQueueName,
                     @Value("${aws.notSuspendedSnsTopicArn}") String notSuspendedSnsTopicArn,
                     @Value("${aws.notSuspendedQueueName}") String notSuspendedQueueName,
                     @Value("${aws.suspensionDynamoDbTableName}") String suspensionDynamoDbTableName) {
        this.environment = environment;
        this.metricNamespace = metricNamespace;
        this.suspensionsQueueName = suspensionsQueueName;
        this.notSuspendedSnsTopicArn = notSuspendedSnsTopicArn;
        this.notSuspendedQueueName = notSuspendedQueueName;
        this.suspensionDynamoDbTableName = suspensionDynamoDbTableName;
    }

    public String environment() {
        return environment;
    }

    public String suspensionsQueueName() {
        return suspensionsQueueName;
    }

    public String notSuspendedSnsTopicArn() {
        return notSuspendedSnsTopicArn;
    }

    public String notSuspendedQueueName() {
        return notSuspendedQueueName;
    }

    public String suspensionDynamoDbTableName() { return suspensionDynamoDbTableName; }

    @Bean
    @SuppressWarnings("unused")
    public CloudWatchClient cloudWatchClient() {
        return CloudWatchClient.create();
    }

    public String metricNamespace() {
        return metricNamespace;
    }
}
