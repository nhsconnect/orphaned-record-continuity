import pytest

from awsmesh.mesh import (
    MESH_MESSAGE_TYPE_DATA,
    MESH_STATUS_EVENT_TRANSFER,
    MESH_STATUS_SUCCESS,
    MeshClientNetworkError,
    MeshMessage,
    MissingMeshHeader,
    UnexpectedMessageType,
    UnexpectedStatusEvent,
    UnsuccessfulStatus,
)
from tests.builders.common import a_datetime, a_string
from tests.builders.mesh import (
    TEST_INBOX_URL,
    a_filename,
    a_timestamp,
    build_mex_headers,
    mesh_client_connection_error,
    mesh_client_http_error,
    mock_client_message,
)


def test_calls_acknowledge_on_underlying_client_message():
    client_message = mock_client_message()
    message = MeshMessage(client_message)

    message.acknowledge()

    client_message.acknowledge.assert_called_once()


def test_exposes_message_id():
    message_id = a_string()
    client_message = mock_client_message(message_id=message_id)
    message = MeshMessage(client_message)

    assert message.id == message_id


def test_calls_read_on_underlying_client_message():
    expected_value = "data"

    client_message = mock_client_message()
    client_message.read.return_value = expected_value
    message = MeshMessage(client_message)

    actual_value = message.read(n=43)

    client_message.read.assert_called_once_with(43)
    assert actual_value == expected_value


def test_exposes_filename():
    mocked_timestamp = a_timestamp()
    mocked_filename = a_filename(mocked_timestamp)
    client_message = mock_client_message(mex_headers=build_mex_headers(file_name=mocked_filename))
    message = MeshMessage(client_message)

    assert message.file_name == mocked_filename


def test_exposes_date_delivered():
    date_delivered = a_datetime()
    status_timestamp_header = date_delivered.strftime("%Y%m%d%H%M%S")
    client_message = mock_client_message(
        mex_headers=build_mex_headers(status_timestamp=status_timestamp_header)
    )
    message = MeshMessage(client_message)

    assert message.date_delivered == date_delivered


def test_exposes_sender():
    sender = a_string()
    client_message = mock_client_message(mex_headers=build_mex_headers(**{"from": sender}))
    message = MeshMessage(client_message)

    assert message.sender == sender


def test_exposes_recipient():
    recipient = a_string()
    client_message = mock_client_message(mex_headers=build_mex_headers(to=recipient))
    message = MeshMessage(client_message)

    assert message.recipient == recipient


def test_exposes_headers():
    headers = {"foo": "bar"}
    client_message = mock_client_message(mex_headers=headers)
    message = MeshMessage(client_message)

    assert message.headers == headers


def test_throws_exception_when_status_event_header_is_not_transfer():
    client_message = mock_client_message(mex_headers=build_mex_headers(status_event="COLLECT"))

    message = MeshMessage(client_message)

    with pytest.raises(UnexpectedStatusEvent):
        message.validate()


def test_exception_records_header_when_status_event_header_is_not_transfer():
    status_event_header_value = "COLLECT"
    client_message = mock_client_message(
        mex_headers=build_mex_headers(status_event=status_event_header_value)
    )

    message = MeshMessage(client_message)

    with pytest.raises(UnexpectedStatusEvent) as exception_info:
        message.validate()

    exception = exception_info.value
    assert exception.header_value == status_event_header_value
    assert exception.header_name == "statusevent"
    assert exception.expected_header_value == MESH_STATUS_EVENT_TRANSFER


def test_throws_exception_when_status_success_header_is_not_success():
    client_message = mock_client_message(mex_headers=build_mex_headers(status_success="ERROR"))

    message = MeshMessage(client_message)

    with pytest.raises(UnsuccessfulStatus):
        message.validate()


def test_exception_records_header_when_status_success_header_is_not_success():
    status_success_header_value = "ERROR"
    client_message = mock_client_message(
        mex_headers=build_mex_headers(status_success=status_success_header_value),
    )

    message = MeshMessage(client_message)

    with pytest.raises(UnsuccessfulStatus) as exception_info:
        message.validate()

    exception = exception_info.value
    assert exception.header_value == status_success_header_value
    assert exception.header_name == "statussuccess"
    assert exception.expected_header_value == MESH_STATUS_SUCCESS


def test_throws_exception_when_message_type_header_is_not_data():
    client_message = mock_client_message(mex_headers=build_mex_headers(message_type="REPORT"))

    message = MeshMessage(client_message)

    with pytest.raises(UnexpectedMessageType):
        message.validate()


def test_exception_records_header_when_message_type_header_is_not_data():
    message_type_header_value = "REPORT"
    client_message = mock_client_message(
        mex_headers=build_mex_headers(message_type=message_type_header_value)
    )

    message = MeshMessage(client_message)

    with pytest.raises(UnexpectedMessageType) as exception_info:
        message.validate()

    exception = exception_info.value
    assert exception.header_value == message_type_header_value
    assert exception.header_name == "messagetype"
    assert exception.expected_header_value == MESH_MESSAGE_TYPE_DATA


@pytest.mark.parametrize(
    "status_success_value",
    ["Success", "success", "SUCCESS", "sUccess"],
)
def test_ignores_case_in_status_success_type_header(status_success_value):
    client_message = mock_client_message(
        mex_headers=build_mex_headers(status_success=status_success_value)
    )
    try:
        message = MeshMessage(client_message)
        message.validate()
    except UnsuccessfulStatus:
        pytest.fail("UnsuccessfulStatus was raised when it shouldn't have been")


@pytest.mark.parametrize(
    "status_event_value",
    ["Transfer", "transfer", "TRANSFER", "tRansfer"],
)
def test_ignores_case_in_success_event_type_header(status_event_value):
    client_message = mock_client_message(
        mex_headers=build_mex_headers(status_event=status_event_value)
    )
    try:
        message = MeshMessage(client_message)
        message.validate()
    except UnexpectedStatusEvent:
        pytest.fail("UnexpectedStatusEvent was raised when it shouldn't have been")


@pytest.mark.parametrize(
    "message_type_value",
    ["Data", "data", "DATA", "dAta"],
)
def test_ignores_case_in_message_type_header(message_type_value):
    client_message = mock_client_message(
        mex_headers=build_mex_headers(message_type=message_type_value)
    )
    try:
        message = MeshMessage(client_message)
        message.validate()
    except UnexpectedMessageType:
        pytest.fail("UnexpectedMessageType was raised when it shouldn't have been")


@pytest.mark.parametrize(
    "missing_header_name",
    ["statusevent", "statussuccess", "messagetype"],
)
def test_exception_raised_for_missing_validation_headers(missing_header_name):
    mex_headers = build_mex_headers()
    del mex_headers[missing_header_name]

    client_message = mock_client_message(mex_headers=mex_headers)

    message = MeshMessage(client_message)

    with pytest.raises(MissingMeshHeader) as exception_info:
        message.validate()

    exception = exception_info.value
    assert exception.header_name == missing_header_name


def test_exception_raised_for_missing_file_name_header():
    mex_headers = build_mex_headers()
    del mex_headers["filename"]

    client_message = mock_client_message(mex_headers=mex_headers)

    message = MeshMessage(client_message)

    with pytest.raises(MissingMeshHeader) as exception_info:
        _ = message.file_name

    exception = exception_info.value
    assert exception.header_name == "filename"


def test_exception_raised_for_missing_status_timestamp_header():
    mex_headers = build_mex_headers()
    del mex_headers["statustimestamp"]

    client_message = mock_client_message(mex_headers=mex_headers)

    message = MeshMessage(client_message)

    with pytest.raises(MissingMeshHeader) as exception_info:
        _ = message.date_delivered

    exception = exception_info.value
    assert exception.header_name == "statustimestamp"


def test_exception_raised_for_missing_from_header():
    mex_headers = build_mex_headers()
    del mex_headers["from"]

    client_message = mock_client_message(mex_headers=mex_headers)

    message = MeshMessage(client_message)

    with pytest.raises(MissingMeshHeader) as exception_info:
        _ = message.sender

    exception = exception_info.value
    assert exception.header_name == "from"


def test_exception_raised_for_missing_to_header():
    mex_headers = build_mex_headers()
    del mex_headers["to"]

    client_message = mock_client_message(mex_headers=mex_headers)

    message = MeshMessage(client_message)

    with pytest.raises(MissingMeshHeader) as exception_info:
        _ = message.recipient

    exception = exception_info.value
    assert exception.header_name == "to"


def test_mesh_network_error_raised_when_ack_raises_http_error():
    client_message = mock_client_message(acknowledge_error=mesh_client_http_error())

    message = MeshMessage(client_message)

    with pytest.raises(MeshClientNetworkError) as e:
        message.acknowledge()

    assert str(e.value) == f"400 HTTP Error: Bad request for url: {TEST_INBOX_URL}"


def test_mesh_network_error_raised_when_ack_raises_connection_error():
    client_message = mock_client_message(acknowledge_error=mesh_client_connection_error("an error"))

    message = MeshMessage(client_message)

    with pytest.raises(MeshClientNetworkError) as e:
        message.acknowledge()

    assert str(e.value) == (
        f"ConnectionError received when attempting to connect to: {TEST_INBOX_URL}"
        " caused by: an error"
    )
