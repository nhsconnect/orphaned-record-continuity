package uk.nhs.prm.repo.re_registration.health;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;
import uk.nhs.prm.repo.re_registration.infra.LocalStackAwsConfig;
import uk.nhs.prm.repo.re_registration.metrics.AppConfig;
import uk.nhs.prm.repo.re_registration.metrics.healthprobes.ActiveSuspensionsQueueHealthProbe;
import uk.nhs.prm.repo.re_registration.metrics.healthprobes.ReRegistrationsQueueHealthProbe;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@ContextConfiguration( classes = {
		LocalStackAwsConfig.class, ReRegistrationsQueueHealthProbe.class, AppConfig.class, ActiveSuspensionsQueueHealthProbe.class
})
class ReRegistrationQueueHealthProbeHealthyTests {

	@Autowired
	private ReRegistrationsQueueHealthProbe probe;

	@Autowired
	private ActiveSuspensionsQueueHealthProbe activeSuspensionsQueueHealthProbe;

	@Autowired
	private SqsClient sqsClient;

	@Value("${aws.reRegistrationsQueueName}")
	private String reRegistrationsQueueName;

	@Value("${aws.activeSuspensionsQueueName}")
	private String activeSuspensionsQueueName;

	@Test
	void shouldReturnHealthyWhenTheProbeCanAccessTheQueue() {
		createQueue(reRegistrationsQueueName);
		createQueue(activeSuspensionsQueueName);
		assertThat(probe.isHealthy()).isEqualTo(true);
		assertThat(activeSuspensionsQueueHealthProbe.isHealthy()).isEqualTo(true);
	}

	private void createQueue(String queueName) {
		sqsClient.createQueue(CreateQueueRequest.builder().queueName(queueName).build());
	}

}
