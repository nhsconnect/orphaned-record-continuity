from datetime import datetime
from unittest.mock import MagicMock

import pytest

from awsmesh.s3 import S3Uploader
from awsmesh.uploader import UploaderError
from tests.builders.aws import build_client_error


def test_upload():
    mock_s3_client = MagicMock()
    bucket_name = "test_bucket"
    mesh_message = MagicMock()
    file_name = "a_file_A1BH13.dat"
    mesh_message.file_name = file_name
    mesh_message.date_delivered = datetime(year=2020, month=11, day=2)

    uploader = S3Uploader(mock_s3_client, bucket_name)
    uploader.upload(mesh_message, MagicMock())

    mock_s3_client.upload_fileobj.assert_called_once_with(
        mesh_message, bucket_name, f"2020/11/02/{file_name}"
    )


def test_upload_records_key():
    mock_s3_client = MagicMock()
    bucket_name = "test_bucket"
    mesh_message = MagicMock()
    file_name = "a_file_A1BH13.dat"
    mesh_message.file_name = file_name
    mesh_message.date_delivered = datetime(year=2020, month=11, day=2)
    forward_message_event = MagicMock()

    uploader = S3Uploader(mock_s3_client, bucket_name)

    uploader.upload(mesh_message, forward_message_event)
    expected_key = f"2020/11/02/{file_name}"

    forward_message_event.record_s3_key.assert_called_once_with(expected_key)


def test_replaces_spaces_with_underscore():
    mock_s3_client = MagicMock()
    bucket_name = "test_bucket"
    mesh_message = MagicMock()
    file_name = "a file A1BH13.dat"
    mesh_message.file_name = file_name
    mesh_message.date_delivered = datetime(year=2020, month=11, day=2)

    expected_key = "2020/11/02/a_file_A1BH13.dat"

    uploader = S3Uploader(mock_s3_client, bucket_name)
    uploader.upload(mesh_message, MagicMock())

    mock_s3_client.upload_fileobj.assert_called_once_with(mesh_message, bucket_name, expected_key)


def test_upload_error_raised_when_upload_raises_exception():
    mock_s3_client = MagicMock()
    bucket_name = "test_bucket"
    mesh_message = MagicMock()
    file_name = "a file A1BH13.dat"
    mesh_message.file_name = file_name
    mesh_message.date_delivered = datetime(year=2020, month=11, day=2)

    uploader = S3Uploader(mock_s3_client, bucket_name)
    error_message = "test_error"
    mock_s3_client.upload_fileobj.side_effect = build_client_error(message=error_message)

    with pytest.raises(UploaderError) as e:
        uploader.upload(mesh_message, MagicMock())

    assert error_message in str(e.value)
