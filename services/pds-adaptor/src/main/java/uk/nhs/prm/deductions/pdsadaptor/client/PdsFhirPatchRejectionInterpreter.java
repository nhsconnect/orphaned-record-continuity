package uk.nhs.prm.deductions.pdsadaptor.client;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;

import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
@Component
public class PdsFhirPatchRejectionInterpreter {
    public boolean isRejectionDueToNotMakingChanges(Exception exception) {
        if (!(exception instanceof HttpStatusCodeException)) {
            return false;
        }
        var exceptionWithStatusCode = (HttpStatusCodeException) exception;
        if (!exceptionWithStatusCode.getStatusCode().equals(HttpStatus.BAD_REQUEST)) {
            return false;
        }

        return extractPatchRejectionDiagnostics(exceptionWithStatusCode)
                .contains("Provided patch made no changes to the resource");
    }

    private static String extractPatchRejectionDiagnostics(HttpStatusCodeException exception) {
        try {
            return new JSONObject(exception.getResponseBodyAsString(UTF_8))
                    .getJSONArray("issue")
                    .getJSONObject(0)
                    .getString("diagnostics");

        } catch (JSONException jsonException) {
            log.debug("Patch rejection has no diagnostics");
            return "";
        }
    }
}
