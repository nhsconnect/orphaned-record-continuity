package uk.nhs.prm.deductions.nemseventprocessor.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;


@Configuration
@EnableScheduling
@ComponentScan("uk.nhs.prm.deductions.nemseventprocessor.metrics")
public class EnableSchedulingSpringConfiguration {
}
