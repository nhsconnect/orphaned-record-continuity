package uk.nhs.prm.deductions.nemseventprocessor.config;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

@Configuration
@Slf4j
public class Tracer {

    public static final String TRACE_ID = "traceId";
    public static final String MESH_MESSAGE_ID = "meshMessageId";
    public static final String NEMS_MESSAGE_ID = "nemsMessageId";

    public String startMessageTrace(String originalMessageId) {
        clearMDCContext();
        String traceIdUUID = UUID.randomUUID().toString();
        String traceIdHex = traceIdUUID;
        MDC.put(TRACE_ID, traceIdHex);
        MDC.put(MESH_MESSAGE_ID, originalMessageId);
        return traceIdHex;
    }

    public String getTraceId() {
        return MDC.get("traceId");
    }

    public void setNemsMessageId(String nemsMessageId) {
        MDC.put(NEMS_MESSAGE_ID, nemsMessageId);
    }

    public String getNemsMessageId() {
        return MDC.get(NEMS_MESSAGE_ID);
    }

    private void clearMDCContext() {
        MDC.remove(TRACE_ID);
        MDC.remove(MESH_MESSAGE_ID);
        MDC.remove(NEMS_MESSAGE_ID);
    }
}
