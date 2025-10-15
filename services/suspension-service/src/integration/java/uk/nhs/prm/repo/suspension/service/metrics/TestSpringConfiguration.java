package uk.nhs.prm.repo.suspension.service.metrics;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

@Configuration
@ComponentScan(excludeFilters =
    @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = HealthCheckStatusPublisher.class)
)
public class TestSpringConfiguration {
}
