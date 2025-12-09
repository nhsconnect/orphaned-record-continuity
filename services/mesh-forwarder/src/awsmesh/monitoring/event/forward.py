from awsmesh.mesh import InvalidMeshHeader, MeshMessage, MissingMeshHeader
from awsmesh.monitoring.error import (
    INVALID_MESH_HEADER_ERROR,
    MISSING_MESH_HEADER_ERROR,
    SNS_EMPTY_MESSAGE_ERROR,
    SNS_INVALID_PARAMETER_ERROR,
)
from awsmesh.monitoring.event.base import BaseForwarderEvent

FORWARD_MESSAGE_EVENT = "FORWARD_MESH_MESSAGE"


class ForwardMessageEvent(BaseForwarderEvent):
    def __init__(self, output):
        super().__init__(output, FORWARD_MESSAGE_EVENT)

    def record_message_metadata(self, message: MeshMessage):
        self._fields["messageId"] = message.id
        self._fields["sender"] = message.sender
        self._fields["recipient"] = message.recipient
        self._fields["fileName"] = message.file_name

    def record_s3_key(self, key):
        self._fields["s3Key"] = key

    def record_sns_message_id(self, sns_message_id):
        self._fields["snsMessageId"] = sns_message_id

    def record_sns_empty_message_error(self, message: MeshMessage):
        self._fields["error"] = SNS_EMPTY_MESSAGE_ERROR
        self._fields["messageHeaders"] = message.headers
        self._level = "error"

    def record_invalid_parameter_error(self, error_message):
        self._fields["error"] = SNS_INVALID_PARAMETER_ERROR
        self._fields["errorMessage"] = error_message
        self._level = "error"

    def record_missing_mesh_header(self, exception: MissingMeshHeader):
        self._fields["error"] = MISSING_MESH_HEADER_ERROR
        self._fields["missingHeaderName"] = exception.header_name

    def record_invalid_mesh_header(self, exception: InvalidMeshHeader):
        self._fields["error"] = INVALID_MESH_HEADER_ERROR
        self._fields["headerName"] = exception.header_name
        self._fields["expectedHeaderValue"] = exception.expected_header_value
        self._fields["receivedHeaderValue"] = exception.header_value
