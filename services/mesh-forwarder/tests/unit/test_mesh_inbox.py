from unittest.mock import MagicMock

import pytest

from awsmesh.mesh import MeshClientNetworkError, MeshInbox
from tests.builders.common import a_string
from tests.builders.mesh import (
    TEST_INBOX_URL,
    mesh_client_connection_error,
    mesh_client_http_error,
    mock_client_message,
    mock_mesh_inbox,
)


def test_mailbox_list_messages_returns_list_of_message_ids():
    message_ids = [a_string(), a_string(), a_string()]
    mesh_inbox = mock_mesh_inbox(client_msg_ids=message_ids)

    assert mesh_inbox.list_message_ids() == message_ids


def test_raises_network_error_when_list_messages_has_http_error():
    def mock_list_messages():
        raise mesh_client_http_error()

    client_inbox = MagicMock()
    client_inbox.list_messages = mock_list_messages

    mesh_inbox = MeshInbox(client_inbox)

    with pytest.raises(MeshClientNetworkError) as e:
        mesh_inbox.list_message_ids()

    assert str(e.value) == f"400 HTTP Error: Bad request for url: {TEST_INBOX_URL}"


def test_raises_network_error_when_list_messages_raises_a_connection_error():
    def mock_list_messages():
        raise mesh_client_connection_error("an error")

    client_inbox = MagicMock()
    client_inbox.list_messages = mock_list_messages

    mesh_inbox = MeshInbox(client_inbox)

    with pytest.raises(MeshClientNetworkError) as e:
        mesh_inbox.list_message_ids()

    assert str(e.value) == (
        f"ConnectionError received when attempting to connect to: {TEST_INBOX_URL}"
        " caused by: an error"
    )


def test_mailbox_retrieve_message_returns_mesh_message():
    message_id = a_string()
    mock_mesh_client = MagicMock()
    mock_mesh_client.retrieve_message.return_value = mock_client_message(message_id=message_id)
    mesh_inbox = MeshInbox(mock_mesh_client)

    retrieved_message = mesh_inbox.retrieve_message(message_id)

    assert retrieved_message.id == message_id
    mock_mesh_client.retrieve_message.assert_called_once_with(message_id)


def test_raises_network_error_when_retrieve_message_has_http_error():
    def mock_retrieve_message(message_id):
        raise mesh_client_http_error()

    client_inbox = MagicMock()
    client_inbox.retrieve_message = mock_retrieve_message

    mesh_inbox = MeshInbox(client_inbox)

    with pytest.raises(MeshClientNetworkError) as e:
        mesh_inbox.retrieve_message(a_string())

    assert str(e.value) == f"400 HTTP Error: Bad request for url: {TEST_INBOX_URL}"


def test_raises_network_error_when_retrieve_message_raises_a_connection_error():
    def mock_retrieve_message(message_id):
        raise mesh_client_connection_error("an error")

    client_inbox = MagicMock()
    client_inbox.retrieve_message = mock_retrieve_message

    mesh_inbox = MeshInbox(client_inbox)

    with pytest.raises(MeshClientNetworkError) as e:
        mesh_inbox.retrieve_message(a_string())

    assert str(e.value) == (
        f"ConnectionError received when attempting to connect to: {TEST_INBOX_URL}"
        " caused by: an error"
    )


def test_raises_network_error_when_counting_messages_raises_an_http_error():
    mesh_inbox = mock_mesh_inbox(count_messages_error=mesh_client_http_error())

    with pytest.raises(MeshClientNetworkError) as e:
        mesh_inbox.count_messages()

    assert str(e.value) == f"400 HTTP Error: Bad request for url: {TEST_INBOX_URL}"


def test_raises_network_error_when_counting_messages_raises_a_connection_error():
    mesh_inbox = mock_mesh_inbox(count_messages_error=mesh_client_connection_error("an error"))

    with pytest.raises(MeshClientNetworkError) as e:
        mesh_inbox.count_messages()

    assert str(e.value) == (
        f"ConnectionError received when attempting to connect to: {TEST_INBOX_URL}"
        " caused by: an error"
    )
