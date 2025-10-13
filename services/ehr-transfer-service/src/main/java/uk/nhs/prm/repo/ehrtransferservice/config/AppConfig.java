package uk.nhs.prm.repo.ehrtransferservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;

@Configuration
public class AppConfig {

    private final String environment;
    private final String transferTrackerDbTableName;

    public AppConfig(@Value("${environment}") String environment,
                     @Value("${aws.transferTrackerDbTableName}") String transferTrackerDbTableName) {
        this.environment = environment;
        this.transferTrackerDbTableName = transferTrackerDbTableName;
    }

    public String environment() {
        return environment;
    }

    public String transferTrackerDbTableName() {
        return transferTrackerDbTableName;
    }

    @Bean
    @SuppressWarnings("unused")
    public CloudWatchClient cloudWatchClient() {
        return CloudWatchClient.create();
    }
}