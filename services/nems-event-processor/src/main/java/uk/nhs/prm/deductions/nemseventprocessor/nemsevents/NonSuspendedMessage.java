package uk.nhs.prm.deductions.nemseventprocessor.nemsevents;

import com.google.gson.GsonBuilder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import uk.nhs.prm.deductions.nemseventprocessor.audit.AuditMessageStatus;

@RequiredArgsConstructor
@Data
public class NonSuspendedMessage {
    private final String nemsMessageId;
    private final AuditMessageStatus messageStatus;

    public String toJsonString() {
        return new GsonBuilder().disableHtmlEscaping().create()
            .toJson(this);
    }
}
