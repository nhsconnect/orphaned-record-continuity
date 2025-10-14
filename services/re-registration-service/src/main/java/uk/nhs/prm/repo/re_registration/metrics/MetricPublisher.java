package uk.nhs.prm.repo.re_registration.metrics;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.Dimension;
import software.amazon.awssdk.services.cloudwatch.model.MetricDatum;
import software.amazon.awssdk.services.cloudwatch.model.PutMetricDataRequest;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

@Component
@RequiredArgsConstructor
public class MetricPublisher {
    public final CloudWatchClient cloudWatchClient;
    private final AppConfig config;

    public void publishMetric(String metricName, Double healthValue) {
        ArrayList<Dimension> dimensions = new ArrayList<>();
        dimensions.add(Dimension
                .builder()
                .name("Environment")
                .value(config.environment())
                .build());

        MetricDatum datum = MetricDatum
                .builder()
                .metricName(metricName)
                .value(healthValue)
                .timestamp(awsCompatibleNow())
                .dimensions(dimensions)
                .build();

        publish(datum);
    }

    private void publish(MetricDatum datum) {
        PutMetricDataRequest request =
                PutMetricDataRequest
                        .builder()
                        .namespace("ReRegistrationService")
                        .metricData(datum)
                        .build();

        cloudWatchClient.putMetricData(request);
    }

    // why? here's why: https://forums.aws.amazon.com/thread.jspa?threadID=328321
    private Instant awsCompatibleNow() {
        return Instant.parse(ZonedDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.MILLIS).format(DateTimeFormatter.ISO_INSTANT));
    }
}
