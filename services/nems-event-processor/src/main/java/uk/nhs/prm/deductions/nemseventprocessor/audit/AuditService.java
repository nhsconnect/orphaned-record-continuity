package uk.nhs.prm.deductions.nemseventprocessor.audit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.nhs.prm.deductions.nemseventprocessor.config.Tracer;
import uk.nhs.prm.deductions.nemseventprocessor.nemsevents.NemsEventParser;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final Tracer tracer;
    private final NemsEventParser nemsEventParser;
    private final AuditEventPublisher auditEventPublisher;

    public void extractNemsMessageIdAndPublishAuditMessage(String messageBody) {
        String nemsMessageId = nemsEventParser.extractNemsMessageIdFromStringBody(messageBody);
        tracer.setNemsMessageId(nemsMessageId);
        auditEventPublisher.sendMessage(new AuditMessage(nemsMessageId, messageBody));
    }
}
