package uk.nhs.prm.deductions.nemseventprocessor.metrics;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.*;
import uk.nhs.prm.deductions.nemseventprocessor.config.SnsClientSpringConfiguration;
import uk.nhs.prm.deductions.nemseventprocessor.config.SqsClientSpringConfiguration;

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
@SpringJUnitConfig(ScheduledTestConfig.class)
@TestPropertySource(properties = {"environment = ci"})
@ContextConfiguration(classes = {SnsClientSpringConfiguration.class, SqsClientSpringConfiguration.class, MetricPublisher.class, AppConfig.class})
@ExtendWith(MockitoExtension.class)
class MetricPublisherTest {

    @Autowired
    private MetricPublisher publisher;

    CloudWatchClient cloudWatchClient = CloudWatchClient.create();
    static final double HEALTHY_HEALTH_VALUE = 1.0;

    @Test
    void shouldPutHealthMetricDataIntoCloudWatch() {
        // TODO investigate why this test intermittently fails
        System.out.println("If this test fails on the pipeline, rerun it. It fails intermittently.");

        publisher.publishMetric("Health", HEALTHY_HEALTH_VALUE);

        List<Metric> metrics = fetchMetricsMatching("NemsEventProcessor", "Health");
        assertThat(metrics).isNotEmpty();

        final MetricDataResult[] metricData = new MetricDataResult[1];
        await().atMost(60, TimeUnit.SECONDS).untilAsserted(() -> {
            metricData[0] = fetchRecentMetricData(2, getMetricWhere(metrics, metricHasDimension("Environment", "ci")));
            assertThat(metricData[0].values()).isNotEmpty();
        });

        assertThat(metricData[0].values()).isNotEmpty();
        assertThat(metricData[0].values().get(0)).isEqualTo(HEALTHY_HEALTH_VALUE);
    }

    @NotNull
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

    private MetricDataResult fetchRecentMetricData(int minutesOfRecency, Metric metric) {
        MetricDataQuery dataQuery = MetricDataQuery.builder()
            .id("health_test_query")
            .metricStat(MetricStat.builder()
                .metric(metric)
                .period(1)
                .stat("Minimum")
                .build())
            .returnData(true)
            .build();
        GetMetricDataRequest request = GetMetricDataRequest.builder()
            .startTime(Instant.now().minusSeconds(minutesOfRecency * 60).truncatedTo(ChronoUnit.MINUTES))
            .endTime(Instant.now().truncatedTo(ChronoUnit.MINUTES))
            .metricDataQueries(dataQuery)
            .build();

        List<MetricDataResult> metricDataResults = cloudWatchClient.getMetricData(request).metricDataResults();
        System.out.println("metric data results size: " + metricDataResults.size());
        assertThat(metricDataResults.size()).isEqualTo(1);

        MetricDataResult metricDataResult = metricDataResults.get(0);
        assertThat(metricDataResult.statusCode()).isEqualTo(StatusCode.COMPLETE);
        System.out.println("metric data result status: " + metricDataResult.statusCodeAsString());
        System.out.println("metric data result hasValues: " + metricDataResult.hasValues());
        System.out.println("metric data result hasTimestamps: " + metricDataResult.hasTimestamps());
        return metricDataResult;
    }
}
