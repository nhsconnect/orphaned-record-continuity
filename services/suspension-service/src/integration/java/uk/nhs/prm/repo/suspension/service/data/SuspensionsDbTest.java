package uk.nhs.prm.repo.suspension.service.data;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.nhs.prm.repo.suspension.service.infra.LocalStackAwsConfig;
import uk.nhs.prm.repo.suspension.service.model.LastUpdatedEvent;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@SpringBootTest()
@ContextConfiguration(classes = { LocalStackAwsConfig.class})
@DirtiesContext
public class SuspensionsDbTest {

    @Autowired
    SuspensionsDb suspensionsDb;
    String nhsNumber = "1111111111";
    String lastUpdated = "2017-11-01T15:00:33+00:00";

    @BeforeEach
    public void setUp() {
        suspensionsDb.save(new LastUpdatedEvent(nhsNumber, lastUpdated));
    }

    @Test
    void shouldReadFromDb() {
        var lastUpdatePatientData = suspensionsDb.getByNhsNumber(nhsNumber);
        assertThat(lastUpdatePatientData.getNhsNumber()).isEqualTo(nhsNumber);
        assertThat(lastUpdatePatientData.getLastUpdated()).isEqualTo(lastUpdated);
    }

    @Test
    void shouldUpdateRecord() {
        var newTimestamp = "2018-11-01T15:00:33+00:00";
        suspensionsDb.save(new LastUpdatedEvent(nhsNumber, newTimestamp));
        var lastUpdatePatientData = suspensionsDb.getByNhsNumber(nhsNumber);
        assertThat(lastUpdatePatientData.getNhsNumber()).isEqualTo(nhsNumber);
        assertThat(lastUpdatePatientData.getLastUpdated()).isEqualTo(newTimestamp);
    }

    @Test
    void shouldHandleNhsNumberThatDoesNotExistInDb() {
        var notExistingNhsNumber = "9898989898";
        var lastUpdatePatientData = suspensionsDb.getByNhsNumber(notExistingNhsNumber);
        assertThat(lastUpdatePatientData).isEqualTo(null);
    }
}