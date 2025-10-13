package uk.nhs.prm.repo.ehrtransferservice.models;

import com.google.gson.JsonObject;

public class S3PointerMessage {
    public static final String S3_BUCKET_NAME_PROPERTY = "s3BucketName";
    public static final String S3_KEY_PROPERTY = "s3Key";
    private final String s3BucketName;
    private final String s3Key;

    public S3PointerMessage(JsonObject s3PointerJsonObject) {
        this.s3BucketName = s3PointerJsonObject.get(S3_BUCKET_NAME_PROPERTY).getAsString();
        this.s3Key = s3PointerJsonObject.get(S3_KEY_PROPERTY).getAsString();
    }

    public String getS3BucketName() {
        return s3BucketName;
    }

    public String getS3Key() {
        return s3Key;
    }
}
