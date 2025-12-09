package uk.nhs.prm.deductions.nemseventprocessor.metrics.healthprobes;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.GetTopicAttributesRequest;
import uk.nhs.prm.deductions.nemseventprocessor.metrics.AppConfig;

@Slf4j
@Component
public class UnhandledSnsHealthProbe implements HealthProbe {
    private final AppConfig config;
    private final SnsClient snsClient;

    @Autowired
    public UnhandledSnsHealthProbe(AppConfig config, SnsClient snsClient) {
        this.config = config;
        this.snsClient = snsClient;
    }

    @Override
    public boolean isHealthy() {
        try {
            snsClient.getTopicAttributes(GetTopicAttributesRequest.builder().topicArn(config.unhandledEventsSnsTopicArn()).build());
            return true;
        } catch (RuntimeException exception) {
            log.info("Failed to query SNS topic: " + config.unhandledEventsSnsTopicArn(), exception);
            return false;
        }
    }

}
