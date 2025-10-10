package uk.nhs.prm.deductions.nemseventprocessor.metrics;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.Dimension;
import software.amazon.awssdk.services.cloudwatch.model.PutMetricDataRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MetricPublisherTest {

    @Captor
    private ArgumentCaptor<PutMetricDataRequest> putRequestCaptor;


    @Test
    public void shouldSetHealthMetricDimensionToAppropriateEnvironment() {
        AppConfig config = Mockito.mock(AppConfig.class);
        CloudWatchClient metricsClient = Mockito.mock(CloudWatchClient.class);
        when(config.environment()).thenReturn("performance");

        MetricPublisher metricPublisher = new MetricPublisher(metricsClient, config);
        metricPublisher.publishMetric("Health", 0.0);

        verify(metricsClient).putMetricData(putRequestCaptor.capture());
        Dimension environmentDimension = putRequestCaptor.getValue().metricData().get(0).dimensions().get(0);

        assertThat(environmentDimension.name()).isEqualTo("Environment");
        assertThat(environmentDimension.value()).isEqualTo("performance");
    }
}
