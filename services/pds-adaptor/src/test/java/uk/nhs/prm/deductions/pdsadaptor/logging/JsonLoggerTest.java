package uk.nhs.prm.deductions.pdsadaptor.logging;

import ch.qos.logback.classic.Level;
import net.logstash.logback.marker.RawJsonAppendingMarker;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.slf4j.LoggerFactory.getLogger;
import static uk.nhs.prm.deductions.pdsadaptor.testing.TestLogAppender.addTestLogAppender;

class JsonLoggerTest {

    private Logger log;

    @BeforeEach
    public void createLogger() {
        log = getLogger(JsonLoggerTest.class);
    }

    @Test
    public void shouldStructurallyLogMessageAsInfoWithNamedJsonField() {
        var testLogAppender = addTestLogAppender();

        var json = asJson(new HashMap<>() {
            {
                put("some_code", "yo code");
                put("some_detail", "hey detail");
            }
        });

        JsonLogger.logInfoWithJson(log, "the message", "json_fieldname", json);

        var logged = testLogAppender.getLastLoggedEvent();
        assertThat(logged.getMessage()).isEqualTo("the message");
        assertThat(logged.getLevel()).isEqualTo(Level.INFO);
        assertThat(logged.getMarker()).isInstanceOf(RawJsonAppendingMarker.class);

        var jsonMarker = (RawJsonAppendingMarker) logged.getMarker();
        assertThat(jsonMarker.getFieldName()).isEqualTo("json_fieldname");

        var loggedJson = (String) jsonMarker.getFieldValue();
        assertThat(loggedJson.contains("some_code")).isTrue();
        assertThat(loggedJson.contains("some_detail")).isTrue();
    }

    @Test
    public void shouldRemoveLineBreaksWithinJsonWhenStructurallyLoggingWithJson() {
        var testLogAppender = addTestLogAppender();

        var jsonWithLineBreak = "{\n" +
                "\"a_field\": \"some value\"}";

        JsonLogger.logInfoWithJson(log, "msg", "json_name", jsonWithLineBreak);

        var logged = testLogAppender.getLastLoggedEvent();
        var jsonMarker = (RawJsonAppendingMarker) logged.getMarker();
        var loggedJson = (String) jsonMarker.getFieldValue();
        assertThat(loggedJson.contains("{\"a_field")).isTrue();
    }

    @Test
    public void shouldLogRawResponseWhenNotValidJsonAndLoggingWithJson() {
        var testLogAppender = addTestLogAppender();

        JsonLogger.logInfoWithJson(log, "some message", "it's invalid you know", "invalid-json");

        var logged = testLogAppender.getLastLoggedEvent();

        var jsonMarker = (RawJsonAppendingMarker) logged.getMarker();
        assertThat(jsonMarker.getFieldName()).isEqualTo("it's invalid you know");

        var loggedJson = (String) jsonMarker.getFieldValue();
        assertThat(loggedJson).isEqualTo("invalid-json");
    }

    private String asJson(HashMap<Object, Object> errorResponseBodyContent) {
        return new JSONObject(errorResponseBodyContent).toString();
    }

}