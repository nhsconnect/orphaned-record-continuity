package uk.nhs.prm.repo.suspension.service.suspensionsevents;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;

public class SuspensionEventBuilder {
    public final ImmutableMap.Builder mapBuilder = ImmutableMap.<String, String>builder();

    public SuspensionEventBuilder lastUpdated(String value) {
        return add("lastUpdated", value);
    }

    public SuspensionEventBuilder previousOdsCode(String value) {
        return add("previousOdsCode", value);
    }

    public SuspensionEventBuilder eventType(String value) {
        return add("eventType", value);
    }

    public SuspensionEventBuilder nhsNumber(String value) {
        return add("nhsNumber", value);
    }

    public SuspensionEventBuilder nemsMessageId(String value) {
        return add("nemsMessageId", value);
    }

    public SuspensionEventBuilder environment(String value) {
        return add("environment", value);
    }

    private SuspensionEventBuilder add(String fieldName, String value) {
        mapBuilder.put(fieldName, value);
        return this;
    }

    private String toJson(ImmutableMap<String, String> messageData) {
        try {
            return new ObjectMapper().writeValueAsString(messageData);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public String buildJson() {
        return toJson(mapBuilder.build());
    }
}
