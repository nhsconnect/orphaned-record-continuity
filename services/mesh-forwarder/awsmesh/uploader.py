from typing import Protocol

from awsmesh.mesh import MeshMessage


class UploaderError(Exception):
    pass


class UploadEventMetadata(Protocol):
    def record_s3_key(self, key):
        ...

    def record_sns_message_id(self, sns_message_id):
        ...

    def record_invalid_parameter_error(self, error_message):
        ...

    def record_sns_empty_message_error(self, message):
        ...


class MessageUploader(Protocol):
    def upload(self, message: MeshMessage, upload_event_metadata: UploadEventMetadata):
        ...
