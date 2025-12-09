package uk.nhs.prm.deductions.pdsadaptor.client;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import uk.nhs.prm.deductions.pdsadaptor.client.auth.AuthService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@ExtendWith(MockitoExtension.class)
class AuthenticatingHttpClientTest {

    @Mock
    private AuthService authService;

    @Mock
    private SimpleHttpClient innerHttpClient;

    @InjectMocks
    private AuthenticatingHttpClient authenticatingHttpClient;

    @Test
    void shouldForwardGetRequestAndAddAuthTokenToHeaders() {
        when(authService.getAccessToken()).thenReturn("authorised_token");
        when((innerHttpClient).get(eq("url"), any(), eq(String.class)))
            .thenAnswer(invocation -> {
                HttpHeaders headers = invocation.getArgument(1);
                assertThat(headers.get(AUTHORIZATION).get(0)).isEqualTo("Bearer authorised_token");
                return new ResponseEntity<>("inner response", HttpStatus.OK);
            });

        var actualResponse = authenticatingHttpClient.get("url", new HttpHeaders(), String.class);

        assertThat(actualResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(actualResponse.getBody()).isEqualTo("inner response");
        verifyNoMoreInteractions(authService);
    }

    @Test
    void shouldForwardPatchRequestAndAddAuthTokenToHeaders() {
        var innerResponse = new ResponseEntity<>("inner response", HttpStatus.OK);
        var patchPayload = "patch payload";

        when(authService.getAccessToken()).thenReturn("authorised_token");
        when((innerHttpClient).patch(eq("url"), any(), eq(patchPayload), eq(String.class)))
                .thenAnswer(invocation -> {
                    HttpHeaders headers = invocation.getArgument(1);
                    assertThat(headers.get(AUTHORIZATION).get(0)).isEqualTo("Bearer authorised_token");
                    return innerResponse;
                });

        var actualResponse = authenticatingHttpClient.patch("url", new HttpHeaders(), patchPayload, String.class);

        assertThat(actualResponse).isEqualTo(innerResponse);
        verifyNoMoreInteractions(authService);
    }

    @Test
    void shouldReturnTypedResponseFromInnerHttpGet() {
        var innerResponse = new ResponseEntity<>(42, HttpStatus.OK);

        when((innerHttpClient).get(eq("url"), any(), eq(Integer.class))).thenReturn(innerResponse);

        var actualResponse = authenticatingHttpClient.get("url", new HttpHeaders(), Integer.class);

        assertThat(actualResponse.getBody()).isEqualTo(42);
    }

    @Test
    void shouldReturnTypedResponseFromInnerHttpPatch() {
        var innerResponse = new ResponseEntity<>(420, HttpStatus.OK);

        when((innerHttpClient).patch(eq("url"), any(), eq("patch payload"), eq(Integer.class))).thenReturn(innerResponse);

        var actualResponse = authenticatingHttpClient.patch("url", new HttpHeaders(), "patch payload", Integer.class);

        assertThat(actualResponse.getBody()).isEqualTo(420);
    }

    @Test
    void shouldExtendOriginalHeadersWhenForwardingGetRequest() {
        HttpHeaders originalHeaders = new HttpHeaders();
        originalHeaders.add("cheese", "blue");
        originalHeaders.add("sneeze", "atchoo");

        when(authService.getAccessToken()).thenReturn("token");
        when((innerHttpClient).get(any(), any(), any()))
                .thenAnswer(invocation -> {
                    HttpHeaders headers = invocation.getArgument(1);
                    assertThat(headers.getFirst("cheese")).contains("blue");
                    assertThat(headers.getFirst("sneeze")).contains("atchoo");
                    assertThat(headers.getFirst(AUTHORIZATION)).contains("token");
                    assertThat(headers.size()).isEqualTo(3);
                    return new ResponseEntity<>("boo", HttpStatus.OK);
                });

        authenticatingHttpClient.get("url", originalHeaders, String.class);
    }

    @Test
    void shouldExtendOriginalHeadersWhenForwardingPatchRequest() {
        HttpHeaders originalHeaders = new HttpHeaders();
        originalHeaders.add("foo", "bar");

        when(authService.getAccessToken()).thenReturn("token");
        when((innerHttpClient).patch(any(), any(), any(), any()))
                .thenAnswer(invocation -> {
                    HttpHeaders headers = invocation.getArgument(1);
                    assertThat(headers.getFirst("foo")).contains("bar");
                    return new ResponseEntity<>("", HttpStatus.OK);
                });

        authenticatingHttpClient.patch("url", originalHeaders, "yep", String.class);
    }

    @Test
    void shouldUseNewAccessTokenIfFirstGetReturns401Status() {
        var okResponse = new ResponseEntity<>("someResponse", HttpStatus.OK);

        when(authService.getAccessToken()).thenReturn("expired_token");
        when(authService.getNewAccessToken()).thenReturn("new_token");

        when((innerHttpClient).get(eq("url"), any(), eq(String.class)))
                .thenAnswer(invocation -> {
                    HttpHeaders headers = invocation.getArgument(1);
                    assertThat(headers.getFirst(AUTHORIZATION)).isEqualTo("Bearer expired_token");
                    throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED);
                })
                .thenAnswer(invocation -> {
                    HttpHeaders headers = invocation.getArgument(1);
                    assertThat(headers.getFirst(AUTHORIZATION)).isEqualTo("Bearer new_token");
                    return okResponse;
                });

        var response = authenticatingHttpClient.get("url", new HttpHeaders(), String.class);
        verify(innerHttpClient, times(2)).get(eq("url"), any(), eq(String.class));

        assertThat(response).isEqualTo(okResponse);
    }

    @Test
    void shouldUseNewAccessTokenIfFirstPatchReturns401Status() {
        var okResponse = new ResponseEntity<>("someResponse", HttpStatus.OK);

        when(authService.getAccessToken()).thenReturn("expired_token");
        when(authService.getNewAccessToken()).thenReturn("new_token");

        when((innerHttpClient).patch(eq("url"), any(), eq("the patch"), eq(String.class)))
                .thenAnswer(invocation -> {
                    HttpHeaders headers = invocation.getArgument(1);
                    assertThat(headers.getFirst(AUTHORIZATION)).isEqualTo("Bearer expired_token");
                    throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED);
                })
                .thenAnswer(invocation -> {
                    HttpHeaders headers = invocation.getArgument(1);
                    assertThat(headers.getFirst(AUTHORIZATION)).isEqualTo("Bearer new_token");
                    return okResponse;
                });

        var response = authenticatingHttpClient.patch("url", new HttpHeaders(), "the patch", String.class);
        verify(innerHttpClient, times(2)).patch(eq("url"), any(), eq("the patch"), eq(String.class));

        assertThat(response).isEqualTo(okResponse);
    }

    @Test
    void shouldThrowHttpStatusExceptionIfGetReturnsAFailureOtherThanUnauthorized() {
        when(innerHttpClient.get(any(), any(), any())).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        assertThrows(HttpStatusCodeException.class, () -> authenticatingHttpClient.get("url", new HttpHeaders(), String.class));
    }

    @Test
    void shouldThrowHttpStatusExceptionIfPatchReturnsAFailureOtherThanUnauthorized() {
        when(innerHttpClient.patch(any(), any(), any(), any())).thenThrow(new HttpClientErrorException(HttpStatus.EXPECTATION_FAILED));

        assertThrows(HttpStatusCodeException.class, () -> authenticatingHttpClient.patch("url", new HttpHeaders(), "boom", String.class));
    }

    @Test
    void shouldStopTryingToRecoverAndThrowUnauthorizedErrorIfGetUnauthorizedAfterRefreshingTokenOnGet() {
        when(innerHttpClient.get(any(), any(), any())).thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

        assertThrows(HttpStatusCodeException.class, () -> authenticatingHttpClient.get("url", new HttpHeaders(), String.class));

        verify(innerHttpClient, times(2)).get(any(), any(), any());
    }

    @Test
    void shouldStopTryingToRecoverAndThrowUnauthorizedErrorIfGetUnauthorizedAfterRefreshingTokenOnPatch() {
        when(innerHttpClient.patch(any(), any(), any(), any())).thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

        assertThrows(HttpStatusCodeException.class, () -> authenticatingHttpClient.patch("url", new HttpHeaders(), "boom and bust", String.class));

        verify(innerHttpClient, times(2)).patch(any(), any(), any(), any());
    }

    @Test
    void shouldThrowAClientErrorOnGetAfterRefreshedToken() {
        when(innerHttpClient.get(any(), any(), any()))
                .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        assertThrows(HttpStatusCodeException.class, () -> authenticatingHttpClient.get("url", new HttpHeaders(), String.class));

        verify(innerHttpClient, times(2)).get(any(), any(), any());
    }

    @Test
    void shouldThrowAClientErrorOnPatchAfterRefreshedToken() {
        when(innerHttpClient.patch(any(), any(), any(), any()))
                .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        assertThrows(HttpStatusCodeException.class, () -> authenticatingHttpClient.patch("url", new HttpHeaders(), "boom and blow", String.class));

        verify(innerHttpClient, times(2)).patch(any(), any(), any(), any());
    }
}
