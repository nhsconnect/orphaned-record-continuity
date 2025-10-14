package uk.nhs.prm.deductions.nemseventprocessor.metrics;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.nhs.prm.deductions.nemseventprocessor.metrics.healthprobes.HealthProbe;

import java.util.List;

@Component
@Slf4j
public class HealthMetricPublisher {

    private static final int SECONDS = 1000;
    private static final int MINUTE_INTERVAL = 60 * SECONDS;
    public static final String HEALTH_METRIC_NAME = "Health";
    List<HealthProbe> allHealthProbes;
    private final MetricPublisher metricPublisher;

    @Autowired
    public HealthMetricPublisher(MetricPublisher metricPublisher, List<HealthProbe> allHealthProbes) {
        this.metricPublisher = metricPublisher;
        this.allHealthProbes = allHealthProbes;
    }


    @Scheduled(fixedRate = MINUTE_INTERVAL)
    public void publishHealthStatus() {
        if (allProbesHealthy()) {
            metricPublisher.publishMetric(HEALTH_METRIC_NAME, 1.0);
        } else {
            metricPublisher.publishMetric(HEALTH_METRIC_NAME, 0.0);
        }
    }

    private boolean allProbesHealthy() {
        boolean allProbesHealthy = true;
        for (HealthProbe healthProbe : allHealthProbes) {
            if (!healthProbe.isHealthy()) {
                allProbesHealthy = false;
                break;
            }
        }
        return allProbesHealthy;
    }
}
