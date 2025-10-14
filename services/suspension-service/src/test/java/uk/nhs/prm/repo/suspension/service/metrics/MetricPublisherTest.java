package uk.nhs.prm.repo.suspension.service.metrics;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.PutMetricDataRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MetricPublisherTest {

    @Mock
    private AppConfig config;

    @Mock
    private CloudWatchClient cloudWatchClient;

    @InjectMocks
    private MetricPublisher metricPublisher;

    @Captor
    private ArgumentCaptor<PutMetricDataRequest> putRequestCaptor;

    @Test
    public void shouldSetHealthMetricDimensionToAppropriateEnvironment() {
        when(config.environment()).thenReturn("local");
        when(config.metricNamespace()).thenReturn("a-metric-namespace");

        metricPublisher.publishMetric("Health", 1.0);

        verify(cloudWatchClient).putMetricData(putRequestCaptor.capture());
        var putMetricDataRequest = putRequestCaptor.getValue();
        var environmentDimension = putMetricDataRequest.metricData().get(0).dimensions().get(0);

        assertThat(putMetricDataRequest.namespace()).isEqualTo("a-metric-namespace");
        assertThat(environmentDimension.name()).isEqualTo("Environment");
        assertThat(environmentDimension.value()).isEqualTo("local");
    }

    @Test
    public void shouldPublisherMetricValuesToCloudwatch() {
        when(config.environment()).thenReturn("local");

        metricPublisher.publishMetric("Health", 1.0);

        verify(cloudWatchClient).putMetricData(putRequestCaptor.capture());
        var metricData = putRequestCaptor.getValue().metricData().get(0);

        assertThat(metricData.value()).isEqualTo(1.0);
        assertThat(metricData.metricName()).isEqualTo("Health");
    }
}