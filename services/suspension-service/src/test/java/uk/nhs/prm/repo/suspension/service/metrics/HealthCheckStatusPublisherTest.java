package uk.nhs.prm.repo.suspension.service.metrics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.repo.suspension.service.metrics.healthprobes.HealthProbe;
import uk.nhs.prm.repo.suspension.service.metrics.healthprobes.NotSuspendedSnsHealthProbe;
import uk.nhs.prm.repo.suspension.service.metrics.healthprobes.SuspensionsQueueHealthProbe;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HealthCheckStatusPublisherTest {
    private MetricPublisher metricPublisher;
    private SuspensionsQueueHealthProbe suspensionsQueueHealthProbe;
    private NotSuspendedSnsHealthProbe notSuspendedSnsHealthProbe;
    private List<HealthProbe> probe = new ArrayList<>();

    @BeforeEach
    void setUp(){
        metricPublisher = Mockito.mock(MetricPublisher.class);
        suspensionsQueueHealthProbe = Mockito.mock(SuspensionsQueueHealthProbe.class);
        notSuspendedSnsHealthProbe = Mockito.mock(NotSuspendedSnsHealthProbe.class);
        probe.add(suspensionsQueueHealthProbe);
        probe.add(notSuspendedSnsHealthProbe);
    }

    @Test
    public void shouldSetHealthMetricToZeroForUnhealthyIfAnyConnectionIsUnhealthy() {
        when(suspensionsQueueHealthProbe.isHealthy()).thenReturn(false);

        HealthCheckStatusPublisher healthPublisher = new HealthCheckStatusPublisher(metricPublisher,probe);
        healthPublisher.publishHealthStatus();

        verify(metricPublisher,times(1)).publishMetric("Health", 0.0);
    }

    @Test
    public void shouldSetHealthMetricToOneIfAllConnectionsAreHealthy() {
        when(suspensionsQueueHealthProbe.isHealthy()).thenReturn(true);
        when(notSuspendedSnsHealthProbe.isHealthy()).thenReturn(true);

        HealthCheckStatusPublisher healthPublisher = new HealthCheckStatusPublisher(metricPublisher, probe);
        healthPublisher.publishHealthStatus();
        verify(metricPublisher,times(1)).publishMetric("Health", 1.0);
    }
}
