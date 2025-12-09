package uk.nhs.prm.deductions.pdsadaptor.client;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

public interface HttpClient {
    <T> ResponseEntity<T> get(String url, HttpHeaders headers, Class<T> responseType);

    <T> ResponseEntity<T> patch(String url, HttpHeaders headers, Object patchPayload, Class<T> responseType);
}
