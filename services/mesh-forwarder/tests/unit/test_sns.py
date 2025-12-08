from unittest.mock import ANY, MagicMock

import pytest

from awsmesh.sns import SNSUploader
from awsmesh.uploader import UploaderError
from tests.builders.aws import build_client_error
from tests.builders.common import a_string


def test_upload_publishes_to_sns():
    mock_sns_client = MagicMock()
    topic_arn = "test_topic"
    mesh_message = MagicMock()
    mesh_message_value = "some_string"
    mesh_message.read.return_value = mesh_message_value.encode("utf-8")

    uploader = SNSUploader(mock_sns_client, topic_arn)
    uploader.upload(mesh_message, MagicMock())

    mock_sns_client.publish.assert_called_once_with(
        TopicArn=topic_arn, Message=mesh_message_value, MessageAttributes=ANY
    )


def test_upload_records_message_id():
    mock_sns_client = MagicMock()
    topic_arn = "test_topic"
    mesh_message = MagicMock()
    forward_message_event = MagicMock()
    message_id = a_string()
    mock_sns_client.publish.return_value = {"MessageId": message_id}

    uploader = SNSUploader(mock_sns_client, topic_arn)
    uploader.upload(mesh_message, forward_message_event)

    forward_message_event.record_sns_message_id.assert_called_once_with(message_id)


def test_upload__will_just_log_and_not_throw__if_there_is_no_message_body_to_upload_rather_than_upload__it_is_probably_an_error_report():
    mock_sns_client = MagicMock()
    empty_mesh_message = MagicMock()
    empty_mesh_message.read.return_value = "".encode("utf-8")

    forward_message_event = MagicMock()

    uploader = SNSUploader(mock_sns_client, "some_topic")
    uploader.upload(empty_mesh_message, forward_message_event)

    mock_sns_client.publish.assert_not_called()
    forward_message_event.record_sns_empty_message_error.assert_called_once_with(empty_mesh_message)


# flake8: noqa: E501
def test_upload__records_error__when_sns_client_raises_invalid_parameter_exception__which_covers_messages_that_are_too_large():
    mock_sns_client = MagicMock()
    forward_message_event = MagicMock()
    mesh_message = MagicMock()

    mock_sns_client.publish.side_effect = build_client_error(
        code="InvalidParameter", message="boom"
    )

    uploader = SNSUploader(mock_sns_client, "some_topic_arn")
    with pytest.raises(UploaderError):
        uploader.upload(mesh_message, forward_message_event)

    forward_message_event.record_invalid_parameter_error.assert_called_once_with("boom")


# flake8: noqa: E501
def test_upload__error_is_raised__when_sns_client_raises_invalid_parameter_exception__which_covers_messages_that_are_too_large():
    mock_sns_client = MagicMock()
    forward_message_event = MagicMock()
    mesh_message = MagicMock()

    mock_sns_client.publish.side_effect = build_client_error(
        code="InvalidParameter", message="boom"
    )

    uploader = SNSUploader(mock_sns_client, "some_topic_arn")

    with pytest.raises(UploaderError) as e:
        uploader.upload(mesh_message, forward_message_event)

    assert "boom" in str(e.value)


def test_upload_error_raised_when_upload_raises_exception():
    mock_sns_client = MagicMock()
    topic_arn = "test_topic"
    mesh_message = MagicMock()
    uploader = SNSUploader(mock_sns_client, topic_arn)
    error_message = "test_error"
    mock_sns_client.publish.side_effect = build_client_error(message=error_message)

    with pytest.raises(UploaderError) as e:
        uploader.upload(mesh_message, MagicMock())

    assert error_message in str(e.value)


def test_upload_forwards_just_the_messageid_mesh_message_as_sns_message_attribute():
    mock_sns_client = MagicMock()
    mesh_message = MagicMock()
    mesh_message.id = "the-message-id"

    expected_sns_message_attributes = {
        "meshMessageId": {"DataType": "String", "StringValue": "the-message-id"}
    }

    uploader = SNSUploader(mock_sns_client, "test_topic")
    uploader.upload(mesh_message, MagicMock())

    mock_sns_client.publish.assert_called_once_with(
        TopicArn=ANY, Message=ANY, MessageAttributes=expected_sns_message_attributes
    )
