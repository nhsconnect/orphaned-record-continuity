package uk.nhs.prm.repo.suspension.service.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;


@Configuration
@EnableScheduling
@ComponentScan("uk.nhs.prm.repo.suspension.service.metrics")
public class EnableSchedulingSpringConfiguration {
}
