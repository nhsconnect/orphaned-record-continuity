package uk.nhs.prm.repo.suspension.service.suspensionsevents;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
@Slf4j
public class SuspensionEventParser {
    private final ObjectMapper mapper = new ObjectMapper();

    public SuspensionEvent parse(String suspensionMessage) throws JsonProcessingException {
        return mapper.readValue(suspensionMessage, SuspensionEvent.class);
    }
}