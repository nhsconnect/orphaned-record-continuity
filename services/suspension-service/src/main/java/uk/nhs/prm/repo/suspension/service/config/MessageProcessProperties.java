package uk.nhs.prm.repo.suspension.service.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
@Setter
public class MessageProcessProperties {
    @Value("${process_only_synthetic_patients}")
    private String processOnlySyntheticPatients;

    @Value("${synthetic_patient_prefix}")
    private String syntheticPatientPrefix;

    @Value("${safe_listed_ods_codes}")
    private String allowedOdsCodes;
}
