from unittest.mock import MagicMock

from awsmesh.mesh import InvalidMeshHeader, MissingMeshHeader
from awsmesh.monitoring.error import (
    INVALID_MESH_HEADER_ERROR,
    MESH_CLIENT_NETWORK_ERROR,
    MISSING_MESH_HEADER_ERROR,
    SNS_EMPTY_MESSAGE_ERROR,
    SNS_INVALID_PARAMETER_ERROR,
    UPLOADER_ERROR,
)
from awsmesh.monitoring.event.forward import FORWARD_MESSAGE_EVENT, ForwardMessageEvent
from awsmesh.uploader import UploaderError
from tests.builders.common import a_string
from tests.builders.mesh import mock_mesh_message


def test_finish_calls_log_event_with_event_name():
    mock_output = MagicMock()

    forward_message_event = ForwardMessageEvent(mock_output)
    forward_message_event.finish()

    mock_output.log_event.assert_called_with(FORWARD_MESSAGE_EVENT, {}, "info")


def test_record_message_metadata():
    mock_output = MagicMock()
    message = mock_mesh_message()

    forward_message_event = ForwardMessageEvent(mock_output)
    forward_message_event.record_message_metadata(message)
    forward_message_event.finish()

    mock_output.log_event.assert_called_with(
        FORWARD_MESSAGE_EVENT,
        {
            "messageId": message.id,
            "sender": message.sender,
            "recipient": message.recipient,
            "fileName": message.file_name,
        },
        "info",
    )


def test_record_s3_key():
    mock_output = MagicMock()
    key = "/a/key"

    forward_message_event = ForwardMessageEvent(mock_output)
    forward_message_event.record_s3_key(key)
    forward_message_event.finish()

    mock_output.log_event.assert_called_with(FORWARD_MESSAGE_EVENT, {"s3Key": key}, "info")


def test_record_sns_message_id():
    mock_output = MagicMock()
    message_id = a_string()

    forward_message_event = ForwardMessageEvent(mock_output)
    forward_message_event.record_sns_message_id(message_id)
    forward_message_event.finish()

    mock_output.log_event.assert_called_with(
        FORWARD_MESSAGE_EVENT, {"snsMessageId": message_id}, "info"
    )


def test_record_sns_invalid_parameter_error():
    mock_output = MagicMock()
    error_message = "test message"

    forward_message_event = ForwardMessageEvent(mock_output)
    forward_message_event.record_invalid_parameter_error(error_message)
    forward_message_event.finish()

    mock_output.log_event.assert_called_with(
        FORWARD_MESSAGE_EVENT,
        {"error": SNS_INVALID_PARAMETER_ERROR, "errorMessage": error_message},
        "error",
    )


def test_record_sns_empty_message_error():
    mock_output = MagicMock()
    empty_message = MagicMock()
    empty_message.headers = {"bobs": "fullhouse"}

    forward_message_event = ForwardMessageEvent(mock_output)
    forward_message_event.record_sns_empty_message_error(empty_message)
    forward_message_event.finish()

    mock_output.log_event.assert_called_with(
        FORWARD_MESSAGE_EVENT,
        {"error": SNS_EMPTY_MESSAGE_ERROR, "messageHeaders": empty_message.headers},
        "error",
    )


def test_record_missing_mesh_header():
    mock_output = MagicMock()
    missing_header_exception = MissingMeshHeader(header_name=a_string())

    forward_message_event = ForwardMessageEvent(mock_output)
    forward_message_event.record_missing_mesh_header(missing_header_exception)
    forward_message_event.finish()

    mock_output.log_event.assert_called_with(
        FORWARD_MESSAGE_EVENT,
        {
            "error": MISSING_MESH_HEADER_ERROR,
            "missingHeaderName": missing_header_exception.header_name,
        },
        "info",
    )


def test_record_invalid_mesh_header():
    mock_output = MagicMock()
    invalid_header_exception = InvalidMeshHeader(
        header_name=a_string(), header_value=a_string(), expected_header_value=a_string()
    )

    forward_message_event = ForwardMessageEvent(mock_output)
    forward_message_event.record_invalid_mesh_header(invalid_header_exception)
    forward_message_event.finish()

    mock_output.log_event.assert_called_with(
        FORWARD_MESSAGE_EVENT,
        {
            "error": INVALID_MESH_HEADER_ERROR,
            "headerName": invalid_header_exception.header_name,
            "expectedHeaderValue": invalid_header_exception.expected_header_value,
            "receivedHeaderValue": invalid_header_exception.header_value,
        },
        "info",
    )


def test_record_mesh_client_network_error():
    mock_output = MagicMock()
    error_message = "Oh no!"
    uploader_error = UploaderError(error_message)

    forward_message_event = ForwardMessageEvent(mock_output)
    forward_message_event.record_mesh_client_network_error(uploader_error)
    forward_message_event.finish()

    mock_output.log_event.assert_called_with(
        FORWARD_MESSAGE_EVENT,
        {"error": MESH_CLIENT_NETWORK_ERROR, "errorMessage": error_message},
        "info",
    )


def test_record_uploader_error():
    mock_output = MagicMock()
    error_message = "Oh no!"
    uploader_error = UploaderError(error_message)

    forward_message_event = ForwardMessageEvent(mock_output)
    forward_message_event.record_uploader_error(uploader_error)
    forward_message_event.finish()

    mock_output.log_event.assert_called_with(
        FORWARD_MESSAGE_EVENT, {"error": UPLOADER_ERROR, "errorMessage": error_message}, "info"
    )
