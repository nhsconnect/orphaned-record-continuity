import json
from datetime import datetime
from logging import LogRecord, makeLogRecord

DEFAULT_LOG_RECORD_ATTRS = vars(makeLogRecord({})).keys()


def _convert_timestamp_to_iso(timestamp: float) -> str:
    return datetime.utcfromtimestamp(timestamp).isoformat()


class JsonFormatter:
    def format(self, record: LogRecord) -> str:
        base = {
            "level": record.levelname,
            "module": record.module,
            "message": record.msg,
            "time": _convert_timestamp_to_iso(record.created),
        }
        extra = {
            attr: getattr(record, attr)
            for attr in vars(record)
            if attr not in DEFAULT_LOG_RECORD_ATTRS
        }

        return json.dumps({**base, **extra})
