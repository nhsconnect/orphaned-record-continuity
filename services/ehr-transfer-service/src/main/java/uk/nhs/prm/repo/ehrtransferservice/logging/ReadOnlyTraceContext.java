package uk.nhs.prm.repo.ehrtransferservice.logging;

import org.slf4j.MDC;

import static uk.nhs.prm.repo.ehrtransferservice.logging.TraceKey.TRACE_ID;

public class ReadOnlyTraceContext {
    public String getTraceId() {
        return MDC.get(TRACE_ID.toString());
    }
}
