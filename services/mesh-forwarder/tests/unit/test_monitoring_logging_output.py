from unittest.mock import MagicMock

from awsmesh.monitoring.output import LoggingOutput


def test_log_event_calls_logger_info_with_event_name_and_fields():
    mock_logger = MagicMock()
    logging_output = LoggingOutput(mock_logger)
    event_name = "New Event"
    event_info = {"field": "field_value"}

    logging_output.log_event(event_name, fields=event_info, level="info")

    expected_extra = {**event_info, "event": event_name}
    mock_logger.info.assert_called_with(f"Observed {event_name}", extra=expected_extra)


def test_log_event_logs_event_name_in_extra_fields():
    mock_logger = MagicMock()
    logging_output = LoggingOutput(mock_logger)
    event_name = "AN_EVENT"

    logging_output.log_event(event_name, {}, "info")

    mock_logger.info.assert_called_with(f"Observed {event_name}", extra={"event": event_name})
