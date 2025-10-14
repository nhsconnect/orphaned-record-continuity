package uk.nhs.prm.repo.suspension.service.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LastUpdatedEvent {
    private String nhsNumber;
    private String lastUpdated;

    public String getNhsNumber() {
        return this.nhsNumber;
    }

    public String getLastUpdated() {
        return this.lastUpdated;
    }
}
