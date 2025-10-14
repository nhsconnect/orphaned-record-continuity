package uk.nhs.prm.deductions.nemseventprocessor.audit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.deductions.nemseventprocessor.config.Tracer;
import uk.nhs.prm.deductions.nemseventprocessor.nemsevents.NemsEventParser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private Tracer tracer;

    @Mock
    private NemsEventParser nemsEventParser;

    @Mock
    private AuditEventPublisher auditEventPublisher;

    @InjectMocks
    private AuditService auditService;

    @Test
    void shouldExtractNemsIdAddToTraceAndPublisheMessage() {
        String messageBody = "someMessageBody";
        String nemsMessageId = "someMessageId";
        when(nemsEventParser.extractNemsMessageIdFromStringBody(messageBody)).thenReturn(nemsMessageId);

        auditService.extractNemsMessageIdAndPublishAuditMessage(messageBody);

        AuditMessage expectedPayload = new AuditMessage(nemsMessageId, messageBody);

        verify(tracer).setNemsMessageId(nemsMessageId);
        verify(auditEventPublisher).sendMessage(expectedPayload);
    }
}
