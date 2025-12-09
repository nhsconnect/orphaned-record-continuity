package uk.nhs.prm.deductions.nemseventprocessor.metrics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.deductions.nemseventprocessor.metrics.healthprobes.HealthProbe;
import uk.nhs.prm.deductions.nemseventprocessor.metrics.healthprobes.SqsHealthProbe;
import uk.nhs.prm.deductions.nemseventprocessor.metrics.healthprobes.SuspensionsSnsHealthProbe;
import uk.nhs.prm.deductions.nemseventprocessor.metrics.healthprobes.UnhandledSnsHealthProbe;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HealthMetricPublisherTest {
private MetricPublisher metricPublisher;
private SqsHealthProbe sqsHealthProbe;
private UnhandledSnsHealthProbe unhandledSnsHealthProbe;
private SuspensionsSnsHealthProbe suspensionsSnsHealthProbe;
private List<HealthProbe> probe = new ArrayList<>();

    @BeforeEach
    void setUp(){
        metricPublisher = Mockito.mock(MetricPublisher.class);
        sqsHealthProbe = Mockito.mock(SqsHealthProbe.class);
        unhandledSnsHealthProbe = Mockito.mock(UnhandledSnsHealthProbe.class);
        suspensionsSnsHealthProbe = Mockito.mock(SuspensionsSnsHealthProbe.class);
        probe.add(sqsHealthProbe);
        probe.add(unhandledSnsHealthProbe);
        probe.add(suspensionsSnsHealthProbe);
    }

    @Test
    public void shouldSetHealthMetricToZeroForUnhealthyIfAnyConnectionIsUnhealthy() {
        when(sqsHealthProbe.isHealthy()).thenReturn(false);

        HealthMetricPublisher healthPublisher = new HealthMetricPublisher(metricPublisher,probe);
        healthPublisher.publishHealthStatus();

        verify(metricPublisher,times(1)).publishMetric("Health", 0.0);
    }

    @Test
    public void shouldSetHealthMetricToOneIfAllConnectionsAreHealthy() {
        when(sqsHealthProbe.isHealthy()).thenReturn(true);
        when(unhandledSnsHealthProbe.isHealthy()).thenReturn(true);
        when(suspensionsSnsHealthProbe.isHealthy()).thenReturn(true);

        HealthMetricPublisher healthPublisher = new HealthMetricPublisher(metricPublisher, probe);
        healthPublisher.publishHealthStatus();
        verify(metricPublisher,times(1)).publishMetric("Health", 1.0);
    }

}
