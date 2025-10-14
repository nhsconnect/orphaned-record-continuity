package uk.nhs.prm.repo.re_registration.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ToggleConfig {

    @Value("${toggle.canSendDeleteEhrRequest}")
    private boolean canSendDeleteEhrRequest;

    public boolean canSendDeleteEhrRequest() {
        return canSendDeleteEhrRequest;
    }
}
