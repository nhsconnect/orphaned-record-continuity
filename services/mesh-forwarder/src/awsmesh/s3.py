from botocore.exceptions import ClientError

from awsmesh.mesh import MeshMessage
from awsmesh.uploader import UploaderError, UploadEventMetadata


class S3Uploader:
    def __init__(self, s3_client, bucket_name: str):
        self._s3_client = s3_client
        self._bucket_name = bucket_name

    def upload(self, message: MeshMessage, upload_event_metadata: UploadEventMetadata):
        try:
            s3_file_name = message.file_name.replace(" ", "_")
            key = f"{message.date_delivered.strftime('%Y/%m/%d')}/{s3_file_name}"
            self._s3_client.upload_fileobj(message, self._bucket_name, key)
            upload_event_metadata.record_s3_key(key)
        except ClientError as e:
            raise UploaderError(str(e))
