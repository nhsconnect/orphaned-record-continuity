package uk.nhs.prm.repo.ehrtransferservice.repo_incoming;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RepoIncomingEventParser {

    private final ObjectMapper mapper = new ObjectMapper();

    public RepoIncomingEvent parse(String repoIncomingMessage) {
        try {
            log.info("Trying to parse repo incoming event");
            return mapper.readValue(repoIncomingMessage, RepoIncomingEvent.class);
        } catch (JsonProcessingException e) {
            log.error("Encountered Exception while trying to parse incoming event message");
            throw new RuntimeException(e);
        }
    }

}