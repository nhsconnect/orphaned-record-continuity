package uk.nhs.prm.repo.ehrtransferservice.handler;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import uk.nhs.prm.repo.ehrtransferservice.activemq.ForceXercesParserExtension;
import uk.nhs.prm.repo.ehrtransferservice.configuration.LocalStackAwsConfig;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.ParsedMessage;
import uk.nhs.prm.repo.ehrtransferservice.models.S3PointerMessage;
import uk.nhs.prm.repo.ehrtransferservice.parsers.S3ExtendedMessageFetcher;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@ExtendWith(ForceXercesParserExtension.class)
@ContextConfiguration(classes = LocalStackAwsConfig.class)
public class S3PointerHandlerTest {

    @Autowired
    private S3Client s3Client;

    @Value("${aws.sqsLargeMessageBucketName}")
    private String sqsLargeMessageBucketName;

    @Autowired
    private S3ExtendedMessageFetcher s3ExtendedMessageFetcher;

    private static final String LARGE_MESSAGE_S3_KEY = UUID.randomUUID().toString();

    @BeforeEach
    void setUp() throws IOException {

        Map<String, String> metadata = new HashMap<>();
        metadata.put("x-amz-meta-myVal", "test");

        PutObjectRequest putOb = PutObjectRequest.builder()
                .bucket(sqsLargeMessageBucketName)
                .key(LARGE_MESSAGE_S3_KEY)
                .metadata(metadata)
                .build();

        s3Client.putObject(putOb,
                RequestBody.fromBytes(Files.readAllBytes(Paths.get("src/integration/resources/data/small-ehr"))));
    }

    @Test
    void shouldReadLargeMessageContentFromS3() throws Exception {
        ParsedMessage parsedMessage = s3ExtendedMessageFetcher.retrieveMessageFromS3(getStaticS3PointerMessage());
        assertThat(parsedMessage.getInteractionId()).isEqualTo("RCMR_IN030000UK06");
    }

    private S3PointerMessage getStaticS3PointerMessage() {
        var validMessage = "{\"s3BucketName\":\"test-s3-bucket-name-cant-have-underscores\",\"s3Key\":\"" + LARGE_MESSAGE_S3_KEY + "\"}";
        var json = (JsonObject) JsonParser.parseString(validMessage);
        return new S3PointerMessage(json);
    }
}
