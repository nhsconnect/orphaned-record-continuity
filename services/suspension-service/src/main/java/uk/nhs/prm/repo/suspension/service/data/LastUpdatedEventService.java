package uk.nhs.prm.repo.suspension.service.data;

import org.springframework.stereotype.Service;
import uk.nhs.prm.repo.suspension.service.model.LastUpdatedEvent;

@Service
public class LastUpdatedEventService {

    private final SuspensionsDb suspensionsDb;

    public LastUpdatedEventService(SuspensionsDb suspensionsDb){
        this.suspensionsDb = suspensionsDb;
    }

    public boolean isOutOfOrder(String nhsNumber, String lastUpdated) {
        var recordFromDb = suspensionsDb.getByNhsNumber(nhsNumber);
        return recordFromDb != null
                && isGreaterOrEqualThan(recordFromDb.getLastUpdated(), lastUpdated);
    }

    public void save(String nhsNumber, String lastUpdated) {
        suspensionsDb.save(new LastUpdatedEvent(nhsNumber, lastUpdated));
    }

    private boolean isGreaterOrEqualThan(String a, String b) {
        return a.compareTo(b) >= 0;
    }
}
