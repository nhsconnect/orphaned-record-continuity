package uk.nhs.prm.repo.re_registration.metrics.healthprobes;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.GetQueueAttributesRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import uk.nhs.prm.repo.re_registration.metrics.AppConfig;

@Component
@Slf4j
public class ActiveSuspensionsQueueHealthProbe implements HealthProbe {
    private final AppConfig config;
    private final SqsClient sqsClient;

    @Autowired
    public ActiveSuspensionsQueueHealthProbe(AppConfig config, SqsClient sqsClient) {
        this.config = config;
        this.sqsClient = sqsClient;
    }

    @Override
    public boolean isHealthy() {
        try {
            var queueUrl = sqsClient.getQueueUrl(GetQueueUrlRequest.builder().queueName(queueName()).build()).queueUrl();
            sqsClient.getQueueAttributes(GetQueueAttributesRequest.builder().queueUrl(queueUrl).build());
            return true;
        } catch (RuntimeException exception) {
            log.info("Failed to query SQS queue: " + queueName(), exception);
            return false;
        }
    }

    private String queueName() {
        return config.activeSuspensionsQueueName();
    }
}
