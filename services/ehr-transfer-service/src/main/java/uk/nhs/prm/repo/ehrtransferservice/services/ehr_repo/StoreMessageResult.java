package uk.nhs.prm.repo.ehrtransferservice.services.ehr_repo;

import uk.nhs.prm.repo.ehrtransferservice.models.confirmmessagestored.StoreMessageResponseBody;

public class StoreMessageResult extends StoreMessageResponseBody {
    public final StoreMessageResponseBody storeMessageResponseBody;

    public StoreMessageResult(StoreMessageResponseBody storeMessageResponseBody) {
        this.storeMessageResponseBody = storeMessageResponseBody;
    }

    public boolean isEhrComplete() {
        return "complete".equals(storeMessageResponseBody.getHealthRecordStatus());
    }
}
