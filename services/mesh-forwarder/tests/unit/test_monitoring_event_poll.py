from unittest.mock import MagicMock

from awsmesh.mesh import MeshClientNetworkError
from awsmesh.monitoring.error import MESH_CLIENT_NETWORK_ERROR
from awsmesh.monitoring.event.poll import POLL_INBOX_EVENT, PollInboxEvent


def test_finish_calls_log_event_with_event_name():
    mock_output = MagicMock()

    poll_inbox_event = PollInboxEvent(mock_output)
    poll_inbox_event.finish()

    mock_output.log_event.assert_called_with(POLL_INBOX_EVENT, {}, "info")


def test_record_message_batch_count():
    mock_output = MagicMock()

    poll_inbox_event = PollInboxEvent(mock_output)
    poll_inbox_event.record_message_batch_count(2)
    poll_inbox_event.finish()

    mock_output.log_event.assert_called_with(POLL_INBOX_EVENT, {"batchMessageCount": 2}, "info")


def test_record_mesh_client_network_error():
    mock_output = MagicMock()
    error_message = "Oh no!"
    mesh_client_network_error = MeshClientNetworkError(error_message)

    poll_inbox_event = PollInboxEvent(mock_output)
    poll_inbox_event.record_mesh_client_network_error(mesh_client_network_error)
    poll_inbox_event.finish()

    mock_output.log_event.assert_called_with(
        POLL_INBOX_EVENT,
        {"error": MESH_CLIENT_NETWORK_ERROR, "errorMessage": error_message},
        "info",
    )
