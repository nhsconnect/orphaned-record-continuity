package uk.nhs.prm.repo.ehrtransferservice.services.ehr_repo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.nhs.prm.repo.ehrtransferservice.exceptions.DuplicateMessageException;
import uk.nhs.prm.repo.ehrtransferservice.exceptions.EhrDeleteRequestException;
import uk.nhs.prm.repo.ehrtransferservice.exceptions.HttpException;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.ParsedMessage;
import uk.nhs.prm.repo.ehrtransferservice.logging.Tracer;
import uk.nhs.prm.repo.ehrtransferservice.models.confirmmessagestored.StoreMessageRequestBody;
import uk.nhs.prm.repo.ehrtransferservice.models.confirmmessagestored.StoreMessageResponseBody;
import uk.nhs.prm.repo.ehrtransferservice.services.PresignedUrl;
import uk.nhs.prm.repo.ehrtransferservice.services.gp2gp_messenger.Gp2gpMessengerService;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

import static uk.nhs.prm.repo.ehrtransferservice.models.enums.AcknowledgementErrorCode.ERROR_CODE_12;

@Slf4j
@Service
public class EhrRepoClient {
    private final String ehrRepoAuthKey;
    private final URL ehrRepoUrl;
    private final Tracer tracer;

    public EhrRepoClient(
            @Value("${ehrRepoUrl}") String ehrRepoUrl,
            @Value("${ehrRepoAuthKey}") String ehrRepoAuthKey,
            Tracer tracer
    ) throws MalformedURLException {
        this.ehrRepoUrl = new URL(ehrRepoUrl);
        this.ehrRepoAuthKey = ehrRepoAuthKey;
        this.tracer = tracer;
    }

    public PresignedUrl fetchStorageUrl(UUID conversationId, UUID messageId) throws DuplicateMessageException, RuntimeException, IOException, URISyntaxException, InterruptedException {
        String endpoint = "/messages/" + conversationId.toString().toUpperCase() + "/" + messageId.toString().toUpperCase();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URL(ehrRepoUrl, endpoint).toURI())
                .header("Authorization", ehrRepoAuthKey)
                .header("Content-Type", "application/json")
                .header("traceId", tracer.getTraceId())
                .GET().build();

        HttpResponse<String> response = HttpClient.newBuilder()
                .build()
                .send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 409) {
            throw new DuplicateMessageException("Tried to store and already existing message in EHR Repo.");
        }
        if (response.statusCode() != 200) {
            throw new RuntimeException("Unexpected response from EHR Repo when retrieving presigned URL");
        }

        URL url = new URL(response.body());
        return new PresignedUrl(url);
    }

    public StoreMessageResponseBody confirmMessageStored(ParsedMessage parsedMessage) throws Exception {
        var endpoint = "/messages";
        var conversationId = parsedMessage.getConversationId();
        var messageId = parsedMessage.getMessageId();
        var messageType = parsedMessage.getInteractionId().equals("RCMR_IN030000UK06") ? "ehrExtract" : "fragment";
        var nhsNumber = parsedMessage.getNhsNumber();
        var fragmentMessageIds = parsedMessage.getFragmentMessageIds();

        var jsonPayloadString = new Gson().toJson(new StoreMessageRequestBody(messageId, conversationId, nhsNumber, messageType, fragmentMessageIds));
        var jsonPayload = HttpRequest.BodyPublishers.ofString(jsonPayloadString);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URL(ehrRepoUrl, endpoint).toURI())
                .header("Authorization", ehrRepoAuthKey)
                .header("Content-Type", "application/json")
                .header("traceId", tracer.getTraceId())
                .POST(jsonPayload).build();

        HttpResponse<String> response;

        try (var httpClient = HttpClient.newHttpClient()) {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception exception) {
            throw new RuntimeException("Error encountered when uploading message to S3", exception);
        }

        if (response.statusCode() != 201) {
            throw new HttpException(String.format("Unexpected response from EHR while checking if a message was stored: %d", response.statusCode()));
        }

        return parseResponse(response);
    }

    public void softDeleteEhrRecord(String nhsNumber) {
        try {
            final String endpoint = String.format("/patients/%s", nhsNumber);
            final HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URL(ehrRepoUrl, endpoint).toURI())
                    .header("Authorization", ehrRepoAuthKey)
                    .header("traceId", tracer.getTraceId())
                    .DELETE()
                    .build();

            final HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new EhrDeleteRequestException(
                        String.format("soft deletion failed with HTTP status %s, and response %s.",
                                response.statusCode(),
                                response.body())
                );
            }
        } catch (IOException | URISyntaxException | InterruptedException | EhrDeleteRequestException exception) {
            log.error(exception.getMessage());
        }
    }

    private static StoreMessageResponseBody parseResponse(HttpResponse<String> response) throws JsonProcessingException {
        return new ObjectMapper().readValue(response.body(), StoreMessageResponseBody.class);
    }
}
