package uk.nhs.prm.repo.re_registration.model;

import com.google.gson.GsonBuilder;
import lombok.*;

@AllArgsConstructor
@Data
public class AuditMessage {

    private String nemsMessageId;
    private String status;

    public String toJsonString() {
        return new GsonBuilder().create().toJson(this);
    }
}
