package uk.nhs.prm.deductions.pdsadaptor.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.UnknownContentTypeException;
import uk.nhs.prm.deductions.pdsadaptor.client.exceptions.PdsFhirPatchSpecifiesNoChangesException;
import uk.nhs.prm.deductions.pdsadaptor.model.UpdateManagingOrganisationRequest;
import uk.nhs.prm.deductions.pdsadaptor.model.pdspatchrequest.PdsPatch;
import uk.nhs.prm.deductions.pdsadaptor.model.pdspatchrequest.PdsPatchRequest;
import uk.nhs.prm.deductions.pdsadaptor.model.pdsresponse.PdsFhirPatient;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static uk.nhs.prm.deductions.pdsadaptor.testing.PdsFhirTestData.buildPdsResponse;
import static uk.nhs.prm.deductions.pdsadaptor.testing.PdsFhirTestData.buildPdsSuspendedResponse;

@ExtendWith(MockitoExtension.class)
class PdsFhirClientTest {

    @Mock
    private AuthenticatingHttpClient httpClient;

    @Mock
    private PdsFhirPatchRejectionInterpreter patchRejectionInterpreter;

    @Mock
    private PdsFhirExceptionHandler exceptionHandler;

    private static final String PDS_FHIR_ENDPOINT = "http://pds-fhir.com/";

    private PdsFhirClient pdsFhirClient;

    private static final String NHS_NUMBER = "123456789";

    private static final String MANAGING_ORGANISATION = "B9087";

    private static final String RECORD_E_TAG = "W/\"1\"";

    private static final String URL_PATH = PDS_FHIR_ENDPOINT + "Patient/" + NHS_NUMBER;

    @Captor
    private ArgumentCaptor<HttpHeaders> headersCaptor;

    @BeforeEach
    void setUp() {
        pdsFhirClient = new PdsFhirClient(httpClient, patchRejectionInterpreter, exceptionHandler, PDS_FHIR_ENDPOINT);
    }

    @Nested
    @DisplayName("PDS FHIR GET Request")
    class PdsFhirGetRequest {
        @Test
        void shouldSetHeaderOnRequestForGet() {
            PdsFhirPatient pdsResponse = buildPdsResponse(NHS_NUMBER, "A1234", LocalDate.now().minusYears(1), null, RECORD_E_TAG);

            when(httpClient.get(eq(URL_PATH), any(), eq(PdsFhirPatient.class))).thenReturn(new ResponseEntity<>(pdsResponse, HttpStatus.OK));

            pdsFhirClient.requestPdsRecordByNhsNumber(NHS_NUMBER);

            verify(httpClient).get(eq(URL_PATH), headersCaptor.capture(), eq(PdsFhirPatient.class));
            assertThat(headersCaptor.getValue().get("X-Request-ID").get(0)).isNotNull();
        }

        @Test
        void shouldReturnPdsResponseWithEtagFromHeaders() {
            PdsFhirPatient pdsResponse = buildPdsResponse(NHS_NUMBER, "A1234", LocalDate.now().minusYears(1), null, null);

            HttpHeaders headers = new HttpHeaders();
            headers.setETag(RECORD_E_TAG);

            when(httpClient.get(eq(URL_PATH), any(), eq(PdsFhirPatient.class))).thenReturn(
                    new ResponseEntity<>(pdsResponse, headers, HttpStatus.OK));

            PdsFhirPatient expected = pdsFhirClient.requestPdsRecordByNhsNumber(NHS_NUMBER);

            assertThat(expected).isEqualTo(pdsResponse);
            assertThat(expected.getETag()).isEqualTo(RECORD_E_TAG);
        }

        @Test
        void shouldDelegateHandlingExceptionsToExceptionHandler() {
            var causingException = new HttpClientErrorException(HttpStatus.BAD_REQUEST);
            var exceptionFromHandler = new RuntimeException("kabooey");

            when(httpClient.get(any(), any(), any())).thenThrow(causingException);
            when(exceptionHandler.handleCommonExceptions("requesting", causingException))
                    .thenThrow(exceptionFromHandler);

            var resultingException = assertThrows(RuntimeException.class, () -> pdsFhirClient.requestPdsRecordByNhsNumber("123"));

            assertThat(resultingException).isEqualTo(exceptionFromHandler);
        }

        @Test
        void shouldDelegateToExceptionHandlerWhenOkResponseButUnexpectedBodyFromPdsFhir() {
            var unknownResponseException = new UnknownContentTypeException(PdsFhirPatient.class, APPLICATION_JSON, 200,
                    "ok", new HttpHeaders(), new byte[0]);
            var exceptionFromHandler = new RuntimeException("kabooey");

            when(httpClient.get(eq(URL_PATH), any(), eq(PdsFhirPatient.class))).thenThrow(unknownResponseException);
            when(exceptionHandler.handleCommonExceptions("requesting", unknownResponseException))
                    .thenThrow(exceptionFromHandler);

            var resultingException = assertThrows(RuntimeException.class, () -> pdsFhirClient.requestPdsRecordByNhsNumber(NHS_NUMBER));

            assertThat(resultingException).isEqualTo(exceptionFromHandler);
        }
    }

    @Nested
    @DisplayName("PDS FHIR Patch Request")
    class PdsFhirPatchRequest {

        @Captor
        private ArgumentCaptor<Object> patchCaptor;

        @Test
        void shouldSetHeadersCorrectlyOnRequestForPatch() {
            var pdsResponse = buildPdsResponse(NHS_NUMBER, "A1234", LocalDate.now().minusYears(1), null, null);
            var requestId = UUID.randomUUID();

            when(httpClient.patch(eq(URL_PATH), any(), any(), eq(PdsFhirPatient.class))).thenReturn(
                    new ResponseEntity<>(pdsResponse, HttpStatus.OK));

            pdsFhirClient.updateManagingOrganisation(NHS_NUMBER, new UpdateManagingOrganisationRequest(MANAGING_ORGANISATION, RECORD_E_TAG), requestId);

            verify(httpClient).patch(eq(URL_PATH), headersCaptor.capture(), patchCaptor.capture(), eq(PdsFhirPatient.class));
            assertThat(headersCaptor.getValue().getFirst("X-Request-ID")).isEqualTo(requestId.toString());
            assertThat(headersCaptor.getValue().getContentType().toString()).isEqualTo("application/json-patch+json");
            assertThat(headersCaptor.getValue().getFirst("If-Match")).isEqualTo(RECORD_E_TAG);
        }

        @Test
        void shouldReturnPdsResponseWithEtagFromHeadersAddedToItAfterSuccessfulUpdate() {
            var tagVersion = "W/\"2\"";
            var managingOrganisation = "A1234";
            var httpClientPdsResponse = buildPdsSuspendedResponse(NHS_NUMBER, managingOrganisation, null);

            var headers = headersWithEtag(tagVersion);

            when(httpClient.patch(eq(URL_PATH), any(), any(), eq(PdsFhirPatient.class))).thenReturn(
                    new ResponseEntity<>(httpClientPdsResponse, headers, HttpStatus.OK));

            var updateResult = pdsFhirClient.updateManagingOrganisation(
                    NHS_NUMBER, new UpdateManagingOrganisationRequest(managingOrganisation, RECORD_E_TAG), aRequestId());

            verify(httpClient).patch(eq(URL_PATH), headersCaptor.capture(), patchCaptor.capture(), eq(PdsFhirPatient.class));
            PdsPatchRequest requestBody = (PdsPatchRequest) patchCaptor.getValue();

            assertThat(requestBody).isNotNull();
            assertThat(requestBody.getPatches()).hasSize(1);

            PdsPatch pdsPatch = requestBody.getPatches().get(0);
            assertThat(pdsPatch.getOp()).isEqualTo("add");
            assertThat(pdsPatch.getPath()).isEqualTo("/managingOrganization");
            assertThat(pdsPatch.getValue().getType()).isEqualTo("Organization");
            assertThat(pdsPatch.getValue().getIdentifier().getSystem()).isEqualTo("https://fhir.nhs.uk/Id/ods-organization-code");
            assertThat(pdsPatch.getValue().getIdentifier().getValue()).isEqualTo(managingOrganisation);

            assertThat(updateResult).isEqualTo(httpClientPdsResponse);
            assertThat(updateResult.getETag()).isEqualTo(tagVersion);
        }

        @Test
        void shouldThrowExceptionWhenPatchUpdateIsToSameManagingOrganisationAndTherebyRejectedAsMakingNoChanges() {
            var httpException = new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Bad Request");

            when(patchRejectionInterpreter.isRejectionDueToNotMakingChanges(httpException)).thenReturn(true);

            when(httpClient.patch(any(), any(), any(), any())).thenThrow(httpException);

            var exception = assertThrows(PdsFhirPatchSpecifiesNoChangesException.class, () -> {
                pdsFhirClient.updateManagingOrganisation(NHS_NUMBER, anUpdateRequest(), aRequestId());
            });

            assertThat(exception.getMessage())
                    .isEqualTo("PDS FHIR request failed status code: 400. reason Provided patch made " +
                            "no changes to the resource");
        }

        @Test
        void shouldDelegateToCommonExceptionHandlerWhenItsA400BadRequestButNotANoChangesRejection() {
            var causingException = new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Bad Request");
            var exceptionFromHandler = new RuntimeException("kaboom");

            when(patchRejectionInterpreter.isRejectionDueToNotMakingChanges(causingException)).thenReturn(false);
            when(httpClient.patch(any(), any(), any(), any())).thenThrow(causingException);
            when(exceptionHandler.handleCommonExceptions("updating", causingException))
                    .thenThrow(exceptionFromHandler);

            var resultingException = assertThrows(RuntimeException.class, () -> pdsFhirClient.updateManagingOrganisation(
                    NHS_NUMBER, anUpdateRequest(), aRequestId()));

            assertThat(resultingException).isEqualTo(exceptionFromHandler);
        }

        @Test
        void shouldDelegateHandlingAllOtherExceptionsToExceptionHandler() {
            var causingException = new HttpClientErrorException(HttpStatus.BAD_REQUEST);
            var exceptionFromHandler = new RuntimeException("kabowza");

            when(httpClient.patch(any(), any(), any(), any())).thenThrow(causingException);
            when(exceptionHandler.handleCommonExceptions("updating", causingException))
                    .thenThrow(exceptionFromHandler);

            var resultingException = assertThrows(RuntimeException.class, () -> pdsFhirClient.updateManagingOrganisation(
                    NHS_NUMBER, anUpdateRequest(), aRequestId()));

            assertThat(resultingException).isEqualTo(exceptionFromHandler);
        }
    }

    private UUID aRequestId() {
        return UUID.randomUUID();
    }

    private HttpHeaders headersWithEtag(String etag) {
        var headers = new HttpHeaders();
        headers.setETag(etag);
        return headers;
    }

    private UpdateManagingOrganisationRequest anUpdateRequest() {
        return new UpdateManagingOrganisationRequest("ODS123", "someTag");
    }
}
