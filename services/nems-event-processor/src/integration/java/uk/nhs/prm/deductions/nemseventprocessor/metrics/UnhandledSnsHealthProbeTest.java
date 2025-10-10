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
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.CreateTopicRequest;
import software.amazon.awssdk.services.sns.model.CreateTopicResponse;
import software.amazon.awssdk.services.sns.model.DeleteTopicRequest;
import uk.nhs.prm.deductions.nemseventprocessor.config.SnsClientSpringConfiguration;
import uk.nhs.prm.deductions.nemseventprocessor.config.SqsClientSpringConfiguration;
import uk.nhs.prm.deductions.nemseventprocessor.metrics.healthprobes.UnhandledSnsHealthProbe;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest()
@ActiveProfiles("test")
@SpringJUnitConfig(ScheduledTestConfig.class)
@TestPropertySource(properties = {"environment = ci"})
@ContextConfiguration(classes = {SnsClientSpringConfiguration.class, SqsClientSpringConfiguration.class, MetricPublisher.class, AppConfig.class})
@ExtendWith(MockitoExtension.class)
class UnhandledSnsHealthProbeTest {

    @Autowired
    private SnsClient snsClient;
    static CreateTopicResponse topic;

    @BeforeAll
    static void setUpTopic(@Autowired SnsClient snsClient) {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("KmsMasterKeyId", "aws/sns");

        String snsTopicName = "integration-test-unhandled-health-probe";
        topic = snsClient.createTopic(CreateTopicRequest.builder().name(snsTopicName).attributes(attributes).build());
    }

    @Test
    void shouldReturnUnhealthyIfCannotQuerySnsTopic() {
        AppConfig config = new AppConfig("int-test", "non-existent-queue", "non-existent-sns-topic", "suspension-non-existent");
        UnhandledSnsHealthProbe unhandledSnsHealthProbe = new UnhandledSnsHealthProbe(config, snsClient);

        assertFalse(unhandledSnsHealthProbe.isHealthy());
    }

    @Test
    void shouldReturnHealthyIfCanQuerySnsTopic() {
        AppConfig config = new AppConfig("int-test", "non-existent-queue", topic.topicArn(), "suspension-non-existent");
        UnhandledSnsHealthProbe unhandledSnsHealthProbe = new UnhandledSnsHealthProbe(config, snsClient);

        assertTrue(unhandledSnsHealthProbe.isHealthy());
    }

    @AfterAll
    static void tearDownTopic(@Autowired SnsClient snsClient) {
        snsClient.deleteTopic(DeleteTopicRequest.builder().topicArn(topic.topicArn()).build());
    }
}