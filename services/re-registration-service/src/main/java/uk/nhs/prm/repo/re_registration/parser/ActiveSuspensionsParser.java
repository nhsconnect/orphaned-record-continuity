package uk.nhs.prm.repo.re_registration.parser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.nhs.prm.repo.re_registration.model.ActiveSuspensionsMessage;

@Component
@Slf4j
public class ActiveSuspensionsParser {
    private final ObjectMapper mapper = new ObjectMapper();

    public ActiveSuspensionsMessage parse(String activeSuspensionsMessage) {
        try {
            log.info("Trying to parse active suspensions message.");
            return mapper.readValue(activeSuspensionsMessage, ActiveSuspensionsMessage.class);
        } catch (JsonProcessingException e) {
            log.error("Encountered Exception while trying to parse active suspensions message.");
            throw new RuntimeException(e);
        }
    }
}
