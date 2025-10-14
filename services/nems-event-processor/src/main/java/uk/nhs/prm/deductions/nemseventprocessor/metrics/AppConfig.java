package uk.nhs.prm.deductions.nemseventprocessor.metrics;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;

@Configuration
public class AppConfig {
    private final String environment;
    private final String incomingQueueName;
    private final String unhandledEventsSnsTopicArn;
    private final String suspensionsSnsTopicArn;

    public AppConfig(@Value("${environment}") String environment, @Value("${aws.nemsEventsQueueName}") String incomingQueueName, @Value("${aws.unhandledEventsSnsTopicArn}") String unhandledEventsSnsTopicArn, @Value("${aws.suspensionsSnsTopicArn}") String suspensionsSnsTopicArn) {
        this.environment = environment;
        this.incomingQueueName = incomingQueueName;
        this.unhandledEventsSnsTopicArn = unhandledEventsSnsTopicArn;
        this.suspensionsSnsTopicArn = suspensionsSnsTopicArn;
    }

    public String environment() {
        return environment;
    }

    public String incomingQueueName() {
        return incomingQueueName;
    }

    public String unhandledEventsSnsTopicArn() {
        return unhandledEventsSnsTopicArn;
    }

    public String suspensionsSnsTopicArn() {
        return suspensionsSnsTopicArn;
    }

    @Bean
    @SuppressWarnings("unused")
    public CloudWatchClient cloudWatchClient() {
        return CloudWatchClient.create();
    }
}
