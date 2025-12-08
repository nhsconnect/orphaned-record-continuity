import logging
from unittest.mock import MagicMock, patch

from awsmesh.monitoring.probe import LoggingProbe


def test_uses_default_logger():
    probe = LoggingProbe()
    logger = logging.getLogger("awsmesh.monitoring.probe")

    with patch.object(logger, "info") as mock_info:
        count_messages_event = probe.new_count_messages_event()
        count_messages_event.finish()

        mock_info.assert_called_once_with(
            "Observed COUNT_MESSAGES", extra={"event": "COUNT_MESSAGES"}
        )


def test_logs_error_for_error_level_event():
    probe = LoggingProbe()
    logger = logging.getLogger("awsmesh.monitoring.probe")

    with patch.object(logger, "error") as mock_error:
        forward_message_event = probe.new_forward_message_event()
        forward_message_event.record_invalid_parameter_error("test error")
        forward_message_event.finish()

        mock_error.assert_called_once_with(
            "Observed FORWARD_MESH_MESSAGE",
            extra={
                "event": "FORWARD_MESH_MESSAGE",
                "error": "SNS_INVALID_PARAMETER_ERROR",
                "errorMessage": "test error",
            },
        )


def test_binds_count_messages_event_to_logger():
    mock_logger = MagicMock()

    probe = LoggingProbe(mock_logger)

    count_messages_event = probe.new_count_messages_event()

    count_messages_event.finish()

    mock_logger.info.assert_called_once_with(
        "Observed COUNT_MESSAGES", extra={"event": "COUNT_MESSAGES"}
    )


def test_binds_forward_message_event_to_logger():
    mock_logger = MagicMock()

    probe = LoggingProbe(mock_logger)

    forward_message_event = probe.new_forward_message_event()

    forward_message_event.finish()

    mock_logger.info.assert_called_once_with(
        "Observed FORWARD_MESH_MESSAGE", extra={"event": "FORWARD_MESH_MESSAGE"}
    )


def test_binds_poll_message_event_to_logger():
    mock_logger = MagicMock()

    probe = LoggingProbe(mock_logger)

    forward_message_event = probe.new_poll_inbox_event()

    forward_message_event.finish()

    mock_logger.info.assert_called_once_with(
        "Observed POLL_MESSAGE", extra={"event": "POLL_MESSAGE"}
    )
