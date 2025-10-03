from unittest.mock import MagicMock

from awsmesh.forwarder import MeshToAwsForwarder


# flake8: noqa: C416
def build_forwarder(**kwargs):
    mock_mesh_inbox = kwargs.get("mesh_inbox", MagicMock())
    mock_uploader = kwargs.get("uploader", MagicMock())
    mock_probe = kwargs.get("probe", MagicMock())
    disable_message_header_validation = kwargs.get("disable_message_header_validation", False)
    mock_uploader.upload.side_effect = kwargs.get("uploader_error", None)
    mock_mesh_inbox.list_message_ids.return_value = [
        m_id for m_id in kwargs.get("list_message_ids", [])
    ]
    mock_mesh_inbox.list_message_ids.side_effect = kwargs.get("list_message_ids_error", None)
    mock_mesh_inbox.retrieve_message.side_effect = kwargs.get("retrieve_message", [])
    mock_mesh_inbox.count_messages.side_effect = kwargs.get("count_error", None)
    mock_mesh_inbox.count_messages.return_value = kwargs.get("inbox_message_count", 0)

    return MeshToAwsForwarder(
        mock_mesh_inbox, mock_uploader, mock_probe, disable_message_header_validation
    )
