package uk.nhs.prm.deductions.pdsadaptor.client;

import net.logstash.logback.marker.RawJsonAppendingMarker;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.UnknownContentTypeException;
import uk.nhs.prm.deductions.pdsadaptor.client.exceptions.*;
import uk.nhs.prm.deductions.pdsadaptor.model.pdsresponse.PdsFhirPatient;

import java.util.HashMap;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static uk.nhs.prm.deductions.pdsadaptor.testing.TestLogAppender.addTestLogAppender;

class PdsFhirExceptionHandlerTest {

    private PdsFhirExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new PdsFhirExceptionHandler();
    }

    @Test
    public void shouldStructurallyLogTheResponseBodyForAHttp404() {
        var testLogAppender = addTestLogAppender();

        var responseBody = new HashMap<>() {
            {
                put("some_code", "yo code");
                put("some_detail", "hey detail");
            }
        };
        assertThrows(RuntimeException.class, () ->
                handler.handleCommonExceptions("some description", createErrorResponse(404, asJson(responseBody))));

        var logged = testLogAppender.getLastLoggedEvent();
        assertThat(logged.getMarker()).isInstanceOf(RawJsonAppendingMarker.class);

        var jsonMarker = (RawJsonAppendingMarker) logged.getMarker();
        assertThat(jsonMarker.getFieldName()).isEqualTo("error_response");

        var loggedJson = (String) jsonMarker.getFieldValue();
        assertThat(loggedJson.contains("some_code")).isTrue();
        assertThat(loggedJson.contains("some_detail")).isTrue();
    }

    @Test
    void shouldThrowBadRequestExceptionWhenPdsResourceInvalid() {
        var badRequest400 = new HttpClientErrorException(BAD_REQUEST, "bad-request-error");

        var exception = assertThrows(BadRequestException.class, () ->
                handler.handleCommonExceptions("context", badRequest400));

        assertThat(exception.getMessage())
                .isEqualTo("Received 400 error from PDS FHIR: error: 400 bad-request-error");
    }

    @Test
    void shouldThrowNotFoundExceptionIfPatientNotFoundInPds() {
        var notFound404 = new HttpClientErrorException(HttpStatus.NOT_FOUND, "error");

        var exception = assertThrows(NotFoundException.class, () ->
                handler.handleCommonExceptions("context", notFound404));

        assertThat(exception.getMessage()).isEqualTo("PDS FHIR Request failed - Patient not found 404");
    }

    @Test
    void shouldThrowAccessTokenRequestExceptionOnForbiddenError___reallyThisIsSuchAComplicatedInteractionAndTotallyUnexpectedSoIfIGetForbiddenFromPdsFhirClientImMeantToAssumeItWasDownToTheAccessTokenRequest__itDoesntSeemReasonable___surelyIfItsSomethingToDoWithReauthenticationItsNothingToDoWithPdsFhirClientPerSeItsAConcernOftheAuthenticatingHttpClient() {
        var forbiddenError403 = new HttpClientErrorException(HttpStatus.FORBIDDEN, "error");

        var exception = assertThrows(AccessTokenRequestException.class, () ->
                handler.handleCommonExceptions("context", forbiddenError403));

        assertThat(exception.getMessage()).contains("Access token request failed");
        assertThat(exception.getCause()).isEqualTo(forbiddenError403);
    }

    @Test
    void shouldThrowAccessTokenRequestExceptionOnUnauthorizedError() {
        var unauthorizedError401 = new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "error");

        var exception = assertThrows(AccessTokenRequestException.class, () ->
                handler.handleCommonExceptions("context", unauthorizedError401));

        assertThat(exception.getMessage()).contains("Access token request failed");
        assertThat(exception.getCause()).isEqualTo(unauthorizedError401);
    }

    @Test
    void shouldThrowTooManyRequestsExceptionWhenExceedingPdsFhirRateLimit() {
        var tooManyRequests429 = new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS, "error");

        var exception = assertThrows(TooManyRequestsException.class, () ->
                handler.handleCommonExceptions("context", tooManyRequests429));

        assertThat(exception.getMessage()).isEqualTo("Rate limit exceeded for PDS FHIR - too many requests");
    }

    @Test
    void whenPdsFhirIsApparentlyUnavailableShouldThrowExceptionDenotingThatItIsPossiblyATemporaryIssueAndThereforeProbablyUsefulToRetry() {
        var pdsServiceIsUnavailable503 = new HttpServerErrorException(HttpStatus.SERVICE_UNAVAILABLE, "error");

        var exception = assertThrows(RetryableRequestException.class, () ->
                handler.handleCommonExceptions("context", pdsServiceIsUnavailable503));

        assertThat(exception.getMessage()).isEqualTo("PDS FHIR request failed status code: 503. reason 503 error");
    }

    @Test
    void whenThereIsANetworkFailureOrTimeoutThrowExceptionThatItIsProbablyUsefulToRetry() {
        var networkFailure = new ResourceAccessException("something like a socket timeout");

        var exception = assertThrows(RetryableRequestException.class, () ->
                handler.handleCommonExceptions("context", networkFailure));

        assertThat(exception.getCause()).isEqualTo(networkFailure);
        assertThat(exception.getMessage()).contains("something like a socket timeout");
    }

    @Test
    void shouldThrowRuntimeExceptionWhenClientCannotParseSeeminglySuccessfulResponse____feelsLikeImplementationDetailLowerDownShouldMoveIntoHttpClient() {
        var unparseableResponseException = new UnknownContentTypeException(PdsFhirPatient.class, APPLICATION_JSON, 200,
                "ok", new HttpHeaders(), new byte[0]);

        var exception = assertThrows(RuntimeException.class, () ->
                handler.handleCommonExceptions("requesting", unparseableResponseException));

        assertThat(exception.getMessage()).contains("PDS FHIR returned unexpected response body when requesting PDS Record");
    }

    @Test
    void shouldRethrowInitialAccessTokenRequestException() {
        var exceptionFromAuthenticationStack = new AccessTokenRequestException(new HttpServerErrorException(HttpStatus.SERVICE_UNAVAILABLE));

        var exception = assertThrows(RuntimeException.class, () ->
                handler.handleCommonExceptions("requesting", exceptionFromAuthenticationStack));

        assertThat(exception).isEqualTo(exceptionFromAuthenticationStack);
    }

    @Test
    void shouldRethrowInitialRuntimeExceptionWhenNotSpecificallyHandled() {
        var unexpectedException = new IllegalArgumentException("not anticipated");

        var exception = assertThrows(RuntimeException.class, () ->
                handler.handleCommonExceptions("requesting", unexpectedException));

        assertThat(exception).isEqualTo(unexpectedException);
    }

    @NotNull
    private HttpClientErrorException createErrorResponse(int statusCode, String responseBodyJson) {
        return new HttpClientErrorException(HttpStatus.resolve(statusCode), "error", responseBodyJson.getBytes(UTF_8), UTF_8);
    }

    private String asJson(HashMap<Object, Object> errorResponseBodyContent) {
        return new JSONObject(errorResponseBodyContent).toString();
    }

}