package uk.nhs.prm.repo.suspension.service.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
public class ToggleConfig {
    @Value("${toggle.canUpdateManagingOrganisationToRepo}")
    private boolean canUpdateManagingOrganisationToRepo;

    @Value("${toggle.repoProcessOnlySafeListedOdsCodes}")
    private boolean repoProcessOnlySafeListedOdsCodes;
}
