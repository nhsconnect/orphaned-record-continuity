package uk.nhs.prm.deductions.nemseventprocessor.config;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import java.util.UUID;

import static java.util.stream.Stream.of;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.nhs.prm.deductions.nemseventprocessor.config.Tracer.*;

class TracerTest {

    private static final String SOME_NEMS_ID = "someNemsId";


    @Test
    void startTraceShouldClearOldValuesAndSetMeshIdAndTraceId() {
        String previousTraceIdInMdc = "previous traceId in MDC";
        MDC.put(TRACE_ID, previousTraceIdInMdc);
        MDC.put(NEMS_MESSAGE_ID, "randomNemsId");
        MDC.put(MESH_MESSAGE_ID, "randomMeshId");

        new Tracer().startMessageTrace("someMeshId");

        assertThat(MDC.get(MESH_MESSAGE_ID)).isEqualTo("someMeshId");
        assertThat(MDC.get(TRACE_ID)).isNotNull();
        assertThat(MDC.get(TRACE_ID)).isNotEqualTo(previousTraceIdInMdc);
        assertThat(MDC.get(NEMS_MESSAGE_ID)).isNull();
    }

    @Test
    void startTraceShouldCreateAUuidTraceIdThatIsHyphenated() {
        var tracer = new Tracer();

        tracer.startMessageTrace("someMeshId");
        var traceId = tracer.getTraceId();

        assertThat(UUID.fromString(traceId)).isNotNull();

        var hyphenSeparatedPartLengths = of(traceId.split("-")).map(part -> part.length());
        assertThat(hyphenSeparatedPartLengths).containsSequence(8, 4, 4, 4, 12);
    }

    @Test
    void shouldAddNemsMessageIdToMDC() {
        new Tracer().setNemsMessageId(SOME_NEMS_ID);
        String mdcValue = MDC.get(NEMS_MESSAGE_ID);
        assertThat(mdcValue).isEqualTo(SOME_NEMS_ID);
    }

    @Test
    void shouldGetNemsMessageIdFromMDC() {
        MDC.put(NEMS_MESSAGE_ID, SOME_NEMS_ID);
        String nemsMessageIdFromTracer = new Tracer().getNemsMessageId();
        assertThat(nemsMessageIdFromTracer).isEqualTo(SOME_NEMS_ID);
    }

    @Test
    void shouldGetTraceIdFromMDC() {
        MDC.put(TRACE_ID, "someTrace");
        String traceId = new Tracer().getTraceId();
        assertThat(traceId).isEqualTo("someTrace");
    }
}
