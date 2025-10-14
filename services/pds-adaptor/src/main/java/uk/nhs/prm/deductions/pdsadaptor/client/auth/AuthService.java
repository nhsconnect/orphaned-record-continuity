package uk.nhs.prm.deductions.pdsadaptor.client.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import uk.nhs.prm.deductions.pdsadaptor.client.exceptions.AccessTokenRequestException;

@Service
@Slf4j
public class AuthService {

    private final SignedJWTGenerator signedJWTGenerator;
    private final RestTemplate restTemplate;
    private final String accessTokenEndpoint;
    private String accessToken = "";

    public AuthService(SignedJWTGenerator signedJWTGenerator,
                       RestTemplate restTemplate,
                       @Value("${accessTokenEndpoint}") String accessTokenEndpoint) {
        this.signedJWTGenerator = signedJWTGenerator;
        this.restTemplate = restTemplate;
        this.accessTokenEndpoint = accessTokenEndpoint;
    }

    public String getNewAccessToken() {
        try {
            HttpEntity<MultiValueMap<String, String>> request = createRequestEntity();
            ResponseEntity<String> accessTokenResponse = restTemplate.postForEntity(accessTokenEndpoint, request, String.class);
            accessToken = getAccessTokenFromResponse(accessTokenResponse);
            log.info("Successfully generated new access token");
            return accessToken;
        } catch (HttpStatusCodeException e) {
            log.error("Got a http exception when requesting new access token", e);
            throw new AccessTokenRequestException(e);
        }
    }

    public String getAccessToken() {
        if (this.accessToken.isEmpty()) {
            log.info("No access token available. Requesting new access token");
            return getNewAccessToken();
        } else {
            return this.accessToken;
        }
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    private HttpEntity<MultiValueMap<String, String>> createRequestEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("grant_type", "client_credentials");
        requestBody.add("client_assertion_type", "urn:ietf:params:oauth:client-assertion-type:jwt-bearer");
        requestBody.add("client_assertion", signedJWTGenerator.createSignedJWT());

        return new HttpEntity<>(requestBody, headers);
    }

    private String getAccessTokenFromResponse(ResponseEntity<String> accessTokenResponse) {
        try {
            var parent = new ObjectMapper().readTree(accessTokenResponse.getBody());
            return parent.get("access_token").asText();
        }
        catch (JsonProcessingException e) {
            throw new AccessTokenRequestException("Could not handle access token response", e);
        }
    }
}