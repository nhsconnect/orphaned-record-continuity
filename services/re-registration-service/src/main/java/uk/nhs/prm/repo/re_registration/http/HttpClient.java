package uk.nhs.prm.repo.re_registration.http;

import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import uk.nhs.prm.repo.re_registration.config.Tracer;

import java.util.Arrays;

@Component
public class HttpClient {

    public static final String AUTHORIZATION = "Authorization";
    private final RestTemplate restTemplate;
    private final Tracer tracer;

    public HttpClient(RestTemplate restTemplate, Tracer tracer) {
        this.restTemplate = restTemplate;
        this.tracer = tracer;
    }

    public ResponseEntity<String> get(String uri, String userName, String password) {
        return restTemplate.exchange(uri, HttpMethod.GET, new HttpEntity<>(getHeaders(userName, password)), String.class);
    }

    public ResponseEntity<String> delete(String uri, String authKey) {
        return restTemplate.exchange(uri, HttpMethod.DELETE, new HttpEntity<>(getHeadersForEhrRepo(authKey)), String.class);
    }

    private HttpHeaders getHeadersForEhrRepo(String authKey) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add(AUTHORIZATION, authKey);
        headers.add("traceId", tracer.getTraceId());
        return headers;
    }

    private MultiValueMap<String, String> createHeader(String authKey) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Content-Type", "application/json");
        headers.add(AUTHORIZATION, authKey);
        headers.add("traceId", tracer.getTraceId());

        return headers;
    }

    private HttpHeaders getHeaders(String userName, String password) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.setBasicAuth(userName, password);
        headers.add("traceId", tracer.getTraceId());
        return headers;
    }
}
