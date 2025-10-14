package uk.nhs.prm.repo.re_registration.metrics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.repo.re_registration.metrics.healthprobes.HealthProbe;
import uk.nhs.prm.repo.re_registration.metrics.healthprobes.ReRegistrationsQueueHealthProbe;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HealthCheckStatusPublisherTest {
    private MetricPublisher metricPublisher;
    private List<HealthProbe> probe = new ArrayList<>();
    private ReRegistrationsQueueHealthProbe reRegistrationsQueueHealthProbe;

    @BeforeEach
    void setUp() {
        metricPublisher = Mockito.mock(MetricPublisher.class);
        reRegistrationsQueueHealthProbe = Mockito.mock(ReRegistrationsQueueHealthProbe.class);
        probe.add(reRegistrationsQueueHealthProbe);
    }

    @Test
    public void shouldSetHealthMetricToZeroForUnhealthyIfAnyConnectionIsUnhealthy() {
        when(reRegistrationsQueueHealthProbe.isHealthy()).thenReturn(false);

        HealthCheckStatusPublisher healthPublisher = new HealthCheckStatusPublisher(metricPublisher, probe);
        healthPublisher.publishHealthStatus();

        verify(metricPublisher, times(1)).publishMetric("Health", 0.0);
    }

    @Test
    public void shouldSetHealthMetricToOneIfAllConnectionsAreHealthy() {
        when(reRegistrationsQueueHealthProbe.isHealthy()).thenReturn(true);

        HealthCheckStatusPublisher healthPublisher = new HealthCheckStatusPublisher(metricPublisher, probe);
        healthPublisher.publishHealthStatus();
        verify(metricPublisher, times(1)).publishMetric("Health", 1.0);
    }

}
