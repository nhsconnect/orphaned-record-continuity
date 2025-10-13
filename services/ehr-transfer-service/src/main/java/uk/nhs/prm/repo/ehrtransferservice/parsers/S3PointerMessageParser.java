package uk.nhs.prm.repo.ehrtransferservice.parsers;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.nhs.prm.repo.ehrtransferservice.models.ParsingResult;
import uk.nhs.prm.repo.ehrtransferservice.models.S3PointerMessage;
import uk.nhs.prm.repo.ehrtransferservice.models.enums.Status;

@Slf4j
@Component
public class S3PointerMessageParser {
    private static final Integer S3_POINTER_MESSAGE_CONTENT_INDEX = 1;
    private static final String S3_POINTER_HEADER = "software.amazon.payloadoffloading.PayloadS3Pointer";

    public ParsingResult<S3PointerMessage> parse(String payload) {
        try {
            var payloadAsJson = JsonParser.parseString(payload);

            if (!isValidS3PointerMessage(payloadAsJson)) {
                log.info("Current message is not a S3PointerMessage");
                return new ParsingResult<>(null, Status.KO);
            }
            var messageContent = payloadAsJson.getAsJsonArray().get(S3_POINTER_MESSAGE_CONTENT_INDEX).getAsJsonObject();

            var s3PointerMessage = new S3PointerMessage(messageContent);
            log.info("Successfully parsed S3PointerMessage");
            return new ParsingResult<>(s3PointerMessage, Status.OK);
        } catch (Exception e) {
            log.error("Error parsing message as S3PointerMessage in ParsingResult.parse");
            return new ParsingResult<>(null, Status.KO);
        }
    }

    private boolean isValidS3PointerMessage(JsonElement payloadAsJson) {
        if (!payloadAsJson.isJsonArray()) {
            return false;
        }

        var payloadAsJsonArray = payloadAsJson.getAsJsonArray();
        if (payloadAsJsonArray.size() != 2) {
            return false;
        }

        var firstObjInArr = payloadAsJsonArray.get(0);
        var secondObjInArr = payloadAsJsonArray.get(1);

        if (!S3_POINTER_HEADER.equals(firstObjInArr.getAsString())) {
            return false;
        }

        if (secondObjInArr.getAsJsonObject().get(S3PointerMessage.S3_BUCKET_NAME_PROPERTY) == null) {
            return false;
        }

        return secondObjInArr.getAsJsonObject().get(S3PointerMessage.S3_KEY_PROPERTY) != null;
    }
}