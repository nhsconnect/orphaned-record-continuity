package uk.nhs.prm.deductions.nemseventprocessor.audit;

import com.google.gson.GsonBuilder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AuditMessage {
    private String nemsMessageId;
    private String messageBody;

    public String toJsonString() {
        return new GsonBuilder().disableHtmlEscaping().create()
            .toJson(this);
    }
}
