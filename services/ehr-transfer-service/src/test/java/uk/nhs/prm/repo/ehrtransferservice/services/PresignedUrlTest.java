package uk.nhs.prm.repo.ehrtransferservice.services;

import com.amazonaws.services.sqs.model.PurgeQueueRequest;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.ParsedMessage;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("unit")
class PresignedUrlTest {
    WireMockServer wireMockServer;

    @BeforeEach
    public void setUp() {
        wireMockServer = initializeWebServer();
    }

    @AfterEach
    public void tearDown() {
        wireMockServer.resetAll();
        wireMockServer.stop();
    }

    private WireMockServer initializeWebServer() {
        final WireMockServer wireMockServer = new WireMockServer(8080);
        wireMockServer.start();
        return wireMockServer;
    }

    @Test
    void shouldUploadMessageToS3() throws IOException, URISyntaxException, InterruptedException {
        URL url = new URL(wireMockServer.baseUrl());
        String messageBody = "test";
        ParsedMessage parsedMessage = new ParsedMessage(null, null, messageBody);
        wireMockServer.stubFor(put(urlEqualTo("/")).willReturn(aResponse().withStatus(200)));

        PresignedUrl presignedUrl = new PresignedUrl(url);
        presignedUrl.uploadMessage(parsedMessage);

        verify(putRequestedFor(urlMatching("/"))
                .withRequestBody(equalTo(messageBody)));
    }

    @Test
    void shouldThrowErrorWhenCannotUploadMessageToS3() throws MalformedURLException {
        URL url = new URL(wireMockServer.baseUrl());
        String messageBody = "test";
        ParsedMessage parsedMessage = new ParsedMessage(null, null, messageBody);
        wireMockServer.stubFor(put(urlEqualTo("/")).willReturn(aResponse().withStatus(503)));

        PresignedUrl presignedUrl = new PresignedUrl(url);
        Exception expected = assertThrows(RuntimeException.class, () ->
                presignedUrl.uploadMessage(parsedMessage)
        );
        assertThat(expected, notNullValue());

        verify(putRequestedFor(urlMatching("/"))
                .withRequestBody(equalTo(messageBody)));
    }
}
