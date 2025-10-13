package uk.nhs.prm.repo.ehrtransferservice.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import uk.nhs.prm.repo.ehrtransferservice.database.enumeration.ConversationTransferStatus;
import uk.nhs.prm.repo.ehrtransferservice.message_publishers.SplunkAuditPublisher;
import uk.nhs.prm.repo.ehrtransferservice.models.SplunkAuditMessage;

import java.util.Optional;
import java.util.UUID;

@Log4j2
@Service
@RequiredArgsConstructor
public class AuditService {
    private final SplunkAuditPublisher splunkAuditPublisher;

    public void publishAuditMessage(
        UUID inboundConversationId,
        ConversationTransferStatus conversationTransferStatus,
        Optional<UUID> nemsMessageId
    ) {
        final SplunkAuditMessage auditMessage = new SplunkAuditMessage(
            inboundConversationId,
            conversationTransferStatus,
            nemsMessageId);

        splunkAuditPublisher.sendMessage(auditMessage);
        log.info("Published audit message with ConversationTransferStatus of: {}.", conversationTransferStatus);
    }
}
