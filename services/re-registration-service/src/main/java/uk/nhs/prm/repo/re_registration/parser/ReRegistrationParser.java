package uk.nhs.prm.repo.re_registration.parser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.nhs.prm.repo.re_registration.model.ReRegistrationEvent;

@Component
@Slf4j
public class ReRegistrationParser {
    private final ObjectMapper mapper = new ObjectMapper();

    public ReRegistrationEvent parse(String reRegistrationMessage) {
        try {
            log.info("Trying to parse re-registration event");
            return mapper.readValue(reRegistrationMessage, ReRegistrationEvent.class);
        } catch (JsonProcessingException e) {
            log.error("Encountered Exception while trying to parse re-registration message");
            throw new RuntimeException(e);
        }
    }
}
