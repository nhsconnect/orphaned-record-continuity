package uk.nhs.prm.repo.suspension.service.metrics;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.*;
import uk.nhs.prm.repo.suspension.service.infra.LocalStackAwsConfig;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest()
@ActiveProfiles("test")
@SpringJUnitConfig(TestSpringConfiguration.class)
@TestPropertySource(properties = {"environment = local", "metricNamespace = SuspensionService"})
@ExtendWith(MockitoExtension.class)
@ContextConfiguration(classes = {LocalStackAwsConfig.class})
@DirtiesContext
public class MetricPublisherIntegrationTest {

    @Autowired
    private MetricPublisher publisher;

    @Autowired
    private CloudWatchClient cloudWatchClient;

    static final double HEALTHY_HEALTH_VALUE = 1.0;

    @Test
    void shouldPutHealthMetricDataIntoCloudWatch() {
        publisher.publishMetric("Health", HEALTHY_HEALTH_VALUE);

        // Wait until the metric appears
        List<Metric> metrics = await()
                .atMost(2, TimeUnit.MINUTES)
                .pollInterval(2, TimeUnit.SECONDS)
                .until(() -> fetchMetricsMatching("SuspensionService", "Health"),
                        list -> list != null && !list.isEmpty());

        Metric metric = metrics.stream()
                .filter(metricHasDimension("Environment", "local"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Metric with Environment=local not found"));

        MetricDataResult[] outMax = new MetricDataResult[1];
        MetricDataResult[] outCnt = new MetricDataResult[1];

        await()
                .atMost(3, TimeUnit.MINUTES)
                .pollInterval(2, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    var results = fetchRecentMetricDataPair(5, metric); // returns [max, sampleCount]
                    outMax[0] = results[0];
                    outCnt[0] = results[1];

                    assertThat(outMax[0].values()).isNotEmpty();
                    assertThat(outCnt[0].values()).isNotEmpty();

                    // Pair by timestamp; consider only buckets with samples
                    boolean seen = false;
                    for (int i = 0; i < outCnt[0].timestamps().size(); i++) {
                        Double cnt = outCnt[0].values().get(i);
                        Instant ts = outCnt[0].timestamps().get(i);
                        // find matching timestamp index in max series
                        int j = outMax[0].timestamps().indexOf(ts);
                        if (j >= 0 && cnt != null && cnt >= 1.0) {
                            Double v = outMax[0].values().get(j);
                            if (v != null && Math.abs(v - HEALTHY_HEALTH_VALUE) < 1e-6) {
                                seen = true;
                                break;
                            }
                        }
                    }
                    assertThat(seen).as("found a non-empty bucket with value ~ 1.0").isTrue();
                });
    }

    private Predicate<Metric> metricHasDimension(String name, String value) {
        return metric -> metric.dimensions().stream().anyMatch(dimension ->
            dimension.name().equals(name) && dimension.value().equals(value));
    }

    private Metric getMetricWhere(List<Metric> metrics, Predicate<Metric> metricPredicate) {
        List<Metric> filteredMetrics = metrics.stream().filter(metricPredicate).collect(toList());
        return filteredMetrics.get(0);
    }
    private List<Metric> fetchMetricsMatching(String namespace, String metricName) {
        ListMetricsRequest request = ListMetricsRequest.builder()
            .namespace(namespace)
            .metricName(metricName)
            .recentlyActive(RecentlyActive.PT3_H)
            .build();

        ListMetricsResponse listMetricsResponse = cloudWatchClient.listMetrics(request);
        return listMetricsResponse.metrics();
    }


    private MetricDataResult[] fetchRecentMetricDataPair(int minutesOfRecency, Metric metric) {
        MetricStat statMax = MetricStat.builder()
                .metric(metric)
                .period(60)
                .stat("Maximum")
                .build();

        MetricStat statCnt = MetricStat.builder()
                .metric(metric)
                .period(60)
                .stat("SampleCount")
                .build();

        MetricDataQuery qMax = MetricDataQuery.builder()
                .id("q_max")
                .metricStat(statMax)
                .returnData(true)
                .build();

        MetricDataQuery qCnt = MetricDataQuery.builder()
                .id("q_cnt")
                .metricStat(statCnt)
                .returnData(true)
                .build();

        Instant now = Instant.now();
        GetMetricDataRequest req = GetMetricDataRequest.builder()
                .startTime(now.minusSeconds(minutesOfRecency * 60L))
                .endTime(now.plusSeconds(90))
                .scanBy(ScanBy.TIMESTAMP_ASCENDING)
                .metricDataQueries(qMax, qCnt)
                .build();

        List<MetricDataResult> res = cloudWatchClient.getMetricData(req).metricDataResults();
        // Expect both series present; status may be PARTIAL while backfilling
        assertThat(res).hasSize(2);
        assertThat(res.get(0).statusCode()).isIn(StatusCode.COMPLETE, StatusCode.PARTIAL_DATA);
        assertThat(res.get(1).statusCode()).isIn(StatusCode.COMPLETE, StatusCode.PARTIAL_DATA);
        return new MetricDataResult[] { res.get(0), res.get(1) };
    }
}
