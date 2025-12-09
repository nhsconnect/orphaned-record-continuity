package uk.nhs.prm.repo.re_registration.model;

import lombok.Getter;
import uk.nhs.prm.repo.re_registration.audit.AuditMessages;

import java.util.List;
import java.util.Objects;

@Getter
public class DeleteAuditMessage extends AuditMessage {

    private final List<String> conversationIds;

    public DeleteAuditMessage(String nemsMessageId, List<String> conversationIds) {
        super(nemsMessageId, AuditMessages.SUCCESSFULLY_DELETED_EHR.status());
        this.conversationIds = conversationIds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        DeleteAuditMessage that = (DeleteAuditMessage) o;
        return Objects.equals(conversationIds, that.conversationIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), conversationIds);
    }
}
