package uk.nhs.prm.deductions.pdsadaptor.configuration;

import lombok.NoArgsConstructor;
import org.slf4j.MDC;
import org.springframework.context.annotation.Configuration;

@Configuration
@NoArgsConstructor
public class Tracer {

    public void setTraceId(String traceId) {
        MDC.put("traceId", traceId);
    }

    public String getTraceId() {
        return MDC.get("traceId");
    }
}
