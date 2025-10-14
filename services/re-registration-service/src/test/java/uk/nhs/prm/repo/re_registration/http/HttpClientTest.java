package uk.nhs.prm.repo.re_registration.http;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;
import uk.nhs.prm.repo.re_registration.config.Tracer;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HttpClientTest {
    @Mock
    private RestTemplate restTemplate;
    @Captor
    ArgumentCaptor<String> uriString;
    @Mock
    Tracer tracer;

    @InjectMocks
    HttpClient client;

    @Test
    void shouldInvokeRestTemplateExchangeWithExpectedUri() {
        String expected_uri = "expected URI";
        when(tracer.getTraceId()).thenReturn(String.valueOf(UUID.randomUUID()));
        client.get(expected_uri,"userName","password");
        verify(restTemplate).exchange(uriString.capture(),any(),any(), ArgumentMatchers.eq(String.class));
        assertEquals(expected_uri,uriString.getValue());
    }
}