package uk.nhs.prm.deductions.pdsadaptor.logging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.util.JSONObjectUtils;
import net.logstash.logback.marker.Markers;
import org.slf4j.Logger;

import java.util.HashMap;

public class JsonLogger {
    public static void logInfoWithJson(Logger log, String message, String jsonFieldName, String json) {
        log.info(Markers.appendRaw(jsonFieldName, withoutLinebreaks(json)), message);
    }

    private static String withoutLinebreaks(String json) {
        var mapper = new ObjectMapper();
        try {
            var data = mapper.readValue(json, HashMap.class);
            return JSONObjectUtils.toJSONString(data);
        }
        catch (JsonProcessingException e) {
            return json;
        }
    }
}
