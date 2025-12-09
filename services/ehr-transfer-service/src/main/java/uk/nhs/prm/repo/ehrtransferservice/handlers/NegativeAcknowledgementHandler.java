package uk.nhs.prm.repo.ehrtransferservice.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.nhs.prm.repo.ehrtransferservice.database.TransferService;
import uk.nhs.prm.repo.ehrtransferservice.models.ack.Acknowledgement;
import uk.nhs.prm.repo.ehrtransferservice.services.AuditService;

import java.util.Optional;
import java.util.UUID;

import static uk.nhs.prm.repo.ehrtransferservice.database.enumeration.ConversationTransferStatus.INBOUND_FAILED;

@Slf4j
@Service
@RequiredArgsConstructor
public class NegativeAcknowledgementHandler {
    private final TransferService transferService;
    private final AuditService auditService;

    private static final String DEFAULT_FAILURE_CODE = "UNKNOWN_ERROR";

    public void handleMessage(Acknowledgement acknowledgement) {
        final UUID inboundConversationId = acknowledgement.getConversationId();
        final Optional<UUID> nemsMessageId = transferService.getNemsMessageIdAsUuid(inboundConversationId);

        logFailureDetail(acknowledgement);

        transferService.updateConversationTransferStatusWithFailure(
            inboundConversationId,
            getFailureCodeForDb(acknowledgement)
        );

        auditService.publishAuditMessage(inboundConversationId, INBOUND_FAILED, nemsMessageId);
    }

    private String getFailureCodeForDb(Acknowledgement acknowledgement) {
        if (acknowledgement.getFailureDetails().isEmpty()) {
            return DEFAULT_FAILURE_CODE;
        }

        return acknowledgement.getFailureDetails().get(0).code();
    }

    private void logFailureDetail(Acknowledgement acknowledgement) {
        final String logTemplate = """
            Negative acknowledgement details:
            
            acknowledgementTypeCode: {},
            detail.code: {},
            detail.displayName: {},
            detail.level: {},
            detail.codeSystem: {}
            """;

        acknowledgement.getFailureDetails().forEach(detail -> log.info(
            logTemplate,
            acknowledgement.getTypeCode(),
            detail.code(),
            detail.displayName(),
            detail.level(),
            detail.codeSystem()
        ));
    }
}