package uk.nhs.prm.repo.ehrtransferservice.parsers;

import org.junit.jupiter.api.Test;
import uk.nhs.prm.repo.ehrtransferservice.models.enums.Status;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.nhs.prm.repo.ehrtransferservice.utils.TestLogAppender.addTestLogAppender;

class S3PointerMessageParserTest {

    @Test
    void shouldParseAValidMessage() {
        var validMessage = "[\"software.amazon.payloadoffloading.PayloadS3Pointer\",{\"s3BucketName\":\"s3-bucket-name\",\"s3Key\":\"s3-key-value\"}]";

        var result = new S3PointerMessageParser().parse(validMessage);

        assertThat(result.getStatus()).isEqualTo(Status.OK);
        assertThat(result.getMessage().getS3BucketName()).isEqualTo("s3-bucket-name");
        assertThat(result.getMessage().getS3Key()).isEqualTo("s3-key-value");
    }

    @Test
    void shouldNotParseWhenMessageIsInvalid() {
        var logged = addTestLogAppender();

        var inValid = "[\"software.amazon.payloadoffloading.PayloadS3Pointer\"]";

        var result = new S3PointerMessageParser().parse(inValid);

        var log = logged.findLoggedEvent("Current message is not a S3PointerMessage");

        assertThat(result.getStatus()).isEqualTo(Status.KO);
        assertThat(result.getMessage()).isNull();
        assertThat(log).isNotNull();
    }

    @Test
    void shouldHandleGracefullyErrorsWithBrokenMessages() {
        var logged = addTestLogAppender();

        var inValid = "not a json, will break parser";

        var result = new S3PointerMessageParser().parse(inValid);

        var err = logged.findLoggedEvent("Error parsing message as S3PointerMessage in ParsingResult.parse");

        assertThat(result.getStatus()).isEqualTo(Status.KO);
        assertThat(result.getMessage()).isNull();
        assertThat(err).isNotNull();
    }
}