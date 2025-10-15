package uk.nhs.prm.repo.suspension.service.pds;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.nhs.prm.repo.suspension.service.model.PdsAdaptorSuspensionStatusResponse;

@Component
@Slf4j
@AllArgsConstructor
public class PdsAdaptorSuspensionStatusResponseParser {
    public PdsAdaptorSuspensionStatusResponse parse(String responseBody) {
        if (null == responseBody) {
            throw new UnexpectedPdsAdaptorResponseException("Response body was null attempting parse PDS status response");
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(responseBody, PdsAdaptorSuspensionStatusResponse.class);
        }
        catch (JsonProcessingException e) {
            log.error("Got an exception while parsing PDS lookup response.");
            throw new UnexpectedPdsAdaptorResponseException("JSON parsing error attempting to parse PDS status response");
        }
    }
}
