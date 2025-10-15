package uk.nhs.prm.repo.suspension.service.http;

import com.google.common.util.concurrent.RateLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitHttpClient {

    @Value("${suspension.pds.adaptor.rate.limit}")
    private String rateLimit;

    private final HttpServiceClient httpServiceClient;

    private RateLimiter rateLimiter;

    @PostConstruct
    private void init(){
        rateLimiter = RateLimiter.create(Double.valueOf(rateLimit));
    }

    public ResponseEntity<String> getWithStatusCodeNoRateLimit(String url, String username, String password) {
        return httpServiceClient.getWithStatusCode(url, username, password);
    }

    public ResponseEntity<String> putWithStatusCodeWithTwoSecRateLimit(String url, String username, String password, Object requestPayload) {
        rateLimiter.acquire();
        return httpServiceClient.putWithStatusCode(url, username, password, requestPayload);
    }
}
