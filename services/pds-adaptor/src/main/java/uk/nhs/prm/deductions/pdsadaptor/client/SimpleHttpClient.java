package uk.nhs.prm.deductions.pdsadaptor.client;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor
@Component
public class SimpleHttpClient implements HttpClient {

    private final RestTemplate restTemplate;

    @Override
    public <T> ResponseEntity<T> get(String url, HttpHeaders headers, Class<T> responseType) {
        return restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), responseType);
    }

    @Override
    public <T> ResponseEntity<T> patch(String url, HttpHeaders headers, Object patchPayload, Class<T> responseType) {
        return restTemplate.exchange(url, HttpMethod.PATCH, new HttpEntity<>(patchPayload, headers), responseType);
    }
}
