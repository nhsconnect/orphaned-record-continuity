package uk.nhs.prm.deductions.nemseventprocessor.metrics;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;
import software.amazon.awssdk.services.sqs.model.CreateQueueResponse;
import software.amazon.awssdk.services.sqs.model.DeleteQueueRequest;
import software.amazon.awssdk.services.sqs.model.QueueAttributeName;
import uk.nhs.prm.deductions.nemseventprocessor.config.SnsClientSpringConfiguration;
import uk.nhs.prm.deductions.nemseventprocessor.config.SqsClientSpringConfiguration;
import uk.nhs.prm.deductions.nemseventprocessor.metrics.healthprobes.SqsHealthProbe;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest()
@ActiveProfiles("test")
@SpringJUnitConfig(ScheduledTestConfig.class)
@TestPropertySource(properties = {"environment = ci"})
@ContextConfiguration(classes = {SnsClientSpringConfiguration.class, SqsClientSpringConfiguration.class, MetricPublisher.class, AppConfig.class})
@ExtendWith(MockitoExtension.class)
public class SqsHealthProbeTest {

    @Autowired
    private SqsClient sqsClient;
    static CreateQueueResponse queue;
    static final String queueName = "integration-test-sqs-health-probe";

    @BeforeAll
    public static void setUpQueue(@Autowired SqsClient sqsClient) {
        Map<QueueAttributeName, String> attributes = new HashMap<>();
        attributes.put(QueueAttributeName.KMS_MASTER_KEY_ID, UUID.randomUUID().toString());

        queue = sqsClient.createQueue(CreateQueueRequest.builder().queueName(queueName).attributes(attributes).build());
    }

    @Test
    void shouldReturnUnhealthyIfCannotQuerySqsQueue() {
        AppConfig config = new AppConfig("int-test", "non-existent-queue", "non-existent-sns-topic", "suspension-non-existent");
        SqsHealthProbe sqsHealthProbe = new SqsHealthProbe(config, sqsClient);

        assertFalse(sqsHealthProbe.isHealthy());
    }

    @Test
    void shouldReturnHealthyIfCanQuerySqsQueue() {
        AppConfig config = new AppConfig("int-test", queueName,"non-existent-sns-topic", "suspension-non-existent");
        SqsHealthProbe sqsHealthProbe = new SqsHealthProbe(config, sqsClient);

        assertTrue(sqsHealthProbe.isHealthy());
    }

     @AfterAll
     static void tearDownQueue(@Autowired SqsClient sqsClient) {
        sqsClient.deleteQueue(DeleteQueueRequest.builder().queueUrl(queue.queueUrl()).build());
    }
}
