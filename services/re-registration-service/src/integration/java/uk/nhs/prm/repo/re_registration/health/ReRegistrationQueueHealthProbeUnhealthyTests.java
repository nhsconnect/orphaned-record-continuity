package uk.nhs.prm.repo.re_registration.health;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import software.amazon.awssdk.services.sqs.SqsClient;
import uk.nhs.prm.repo.re_registration.config.SqsClientSpringConfiguration;
import uk.nhs.prm.repo.re_registration.metrics.AppConfig;
import uk.nhs.prm.repo.re_registration.metrics.MetricPublisher;
import uk.nhs.prm.repo.re_registration.metrics.healthprobes.ReRegistrationsQueueHealthProbe;

import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(classes = {SqsClientSpringConfiguration.class, ReRegistrationsQueueHealthProbe.class, MetricPublisher.class, AppConfig.class})
class ReRegistrationQueueHealthProbeUnhealthyTests {

    @Autowired
    private ReRegistrationsQueueHealthProbe probe;

    @Autowired
    private SqsClient sqsClient;


    @Test
    void shouldReturnNotHealthyWhenTheProbeCannotAccessTheQueue() {
        var appConfig = new AppConfig("int-test", "non-existent-queue", "non-existent-queue", "non-existent-db");
        var probe = new ReRegistrationsQueueHealthProbe(appConfig, sqsClient);

        assertFalse(probe.isHealthy());
    }

}
