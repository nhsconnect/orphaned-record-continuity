package uk.nhs.prm.repo.ehrtransferservice.parsers;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import uk.nhs.prm.repo.ehrtransferservice.models.ack.Acknowledgement;
import uk.nhs.prm.repo.ehrtransferservice.models.ack.AcknowledgementTypeCode;
import uk.nhs.prm.repo.ehrtransferservice.models.ack.FailureLevel;
import uk.nhs.prm.repo.ehrtransferservice.utils.ReadableTestDataHandler;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("unit")
class MCCIUK13AcknowledgementParsingTest {

    private final Parser parser = new Parser();
    private final ReadableTestDataHandler readableReader = new ReadableTestDataHandler();

    @Test
    void shouldNotFailToParseWhenFailedToExtractFailureDetailsFromNegativeAcknowledgement() throws IOException {
        String messageAsString = readableReader.readMessage("MCCI_IN010000UK13", "EmptyFailure");
        var parsedAcknowledgement = (Acknowledgement) parser.parse(messageAsString);

        assertThat(parsedAcknowledgement.getFailureDetails()).isEmpty();
    }

    @Test
    void shouldExtractFailureDetailsFromTheReasonsFor_AE_TypeFailure() throws IOException {
        String messageAsString = readableReader.readMessage("MCCI_IN010000UK13", "AE_TypeFailure");
        var parsedAcknowledgement = (Acknowledgement) parser.parse(messageAsString);

        assertThat(parsedAcknowledgement.getTypeCode()).isEqualTo(AcknowledgementTypeCode.AE);

        assertThat(parsedAcknowledgement.getFailureDetails()).hasSize(2);
        var firstReasonFailure = parsedAcknowledgement.getFailureDetails().get(0);
        assertThat(firstReasonFailure.displayName()).isEqualTo("Update Failed");
        assertThat(firstReasonFailure.code()).isEqualTo("15");
        assertThat(firstReasonFailure.codeSystem()).isEqualTo("2.16.840.1.113883.2.1.3.2.4.17.42");
        assertThat(firstReasonFailure.level()).isEqualTo(FailureLevel.WARNING);

        var secondReasonFailure = parsedAcknowledgement.getFailureDetails().get(1);
        assertThat(secondReasonFailure.displayName()).isEqualTo("Update Failed - invalid GP Registration data supplied");
        assertThat(secondReasonFailure.code()).isEqualTo("IU058");
        assertThat(secondReasonFailure.level()).isEqualTo(FailureLevel.ERROR);
    }

    @Test
    void shouldNotFailToParseWhenReasonQualifierHasSomeExtraFieldsOnIt() throws IOException {
        String messageAsString = readableReader.readMessage("MCCI_IN010000UK13", "AE_TypeFailure_ExtraQualifierFields");
        var parsed = (Acknowledgement) parser.parse(messageAsString);

        assertThat(parsed.getFailureDetails().get(0).level()).isEqualTo(FailureLevel.WARNING);
    }

    @Test
    void shouldExtractFailureDetailsFromTheReasonsFor_AR_TypeFailure() throws IOException {
        String messageAsString = readableReader.readMessage("MCCI_IN010000UK13", "AR_TypeFailure");
        var parsedAcknowledgement = (Acknowledgement) parser.parse(messageAsString);

        assertThat(parsedAcknowledgement.getTypeCode()).isEqualTo(AcknowledgementTypeCode.AR);

        assertThat(parsedAcknowledgement.getFailureDetails()).hasSize(2);
        var firstReasonFailure = parsedAcknowledgement.getFailureDetails().get(0);
        assertThat(firstReasonFailure.displayName()).isEqualTo("Access denied");
        assertThat(firstReasonFailure.code()).isEqualTo("1");
        assertThat(firstReasonFailure.codeSystem()).isEqualTo("2.16.840.1.113883.2.1.3.2.4.17.32");
        assertThat(firstReasonFailure.level()).isEqualTo(FailureLevel.ERROR);

        var secondReasonFailure = parsedAcknowledgement.getFailureDetails().get(1);
        assertThat(secondReasonFailure.displayName()).isEqualTo("Made up warning for testing");
        assertThat(secondReasonFailure.code()).isEqualTo("2");
        assertThat(secondReasonFailure.level()).isEqualTo(FailureLevel.WARNING);
    }
}
