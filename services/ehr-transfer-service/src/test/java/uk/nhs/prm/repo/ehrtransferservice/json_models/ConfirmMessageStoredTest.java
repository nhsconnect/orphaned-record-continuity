package uk.nhs.prm.repo.ehrtransferservice.json_models;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import uk.nhs.prm.repo.ehrtransferservice.models.confirmmessagestored.StoreMessageRequestBody;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class ConfirmMessageStoredTest {
    UUID messageId = UUID.randomUUID();
    UUID conversationId = UUID.randomUUID();
    String nhsNumber = "1234567890";
    String messageType = "ehrExtract";
    List<UUID> fragmentMessageIds = new ArrayList<>();
    UUID fragmentMessageID = UUID.randomUUID();
    UUID secondFragmentMessageID = UUID.randomUUID();

    @Test
    void shouldSerializeToJson() {
        String expectedJson = "{\"data\":{\"type\":\"messages\",\"id\":\"" + messageId + "\",\"attributes\":{\"conversationId\":\"" + conversationId + "\",\"messageType\":\"" + messageType + "\",\"nhsNumber\":\"" + nhsNumber + "\",\"fragmentMessageIds\":[]}}}";
        String jsonText = new Gson().toJson(new StoreMessageRequestBody(messageId, conversationId, nhsNumber, messageType, fragmentMessageIds));
        assertThat(jsonText, equalTo(expectedJson));
    }

    @Test
    void shouldSerializeToJsonWithFragmentMessageIds() {
        fragmentMessageIds.add(fragmentMessageID);
        fragmentMessageIds.add(secondFragmentMessageID);

        String expectedJson = "{\"data\":{\"type\":\"messages\",\"id\":\"" + messageId + "\",\"attributes\":{\"conversationId\":\"" + conversationId + "\",\"messageType\":\"" + messageType + "\",\"nhsNumber\":\"" + nhsNumber + "\",\"fragmentMessageIds\":[\"" + fragmentMessageID + "\",\"" + secondFragmentMessageID + "\"]}}}";
        String jsonText = new Gson().toJson(new StoreMessageRequestBody(messageId, conversationId, nhsNumber, messageType, fragmentMessageIds));
        assertThat(jsonText, equalTo(expectedJson));
    }
}
