import json
from logging import makeLogRecord

import pytest

from awsmesh.logging import JsonFormatter
from tests.builders.common import a_string
from tests.builders.mesh import an_epoch_timestamp


def _build_log_record(**kwargs):
    record_fields = {
        "levelname": kwargs.get("levelname", a_string()),
        "module": kwargs.get("module", a_string()),
        "msg": kwargs.get("msg", a_string()),
        "created": kwargs.get("created", an_epoch_timestamp()),
    }
    if "extra" in kwargs:
        record_fields.update(kwargs.get("extra"))
    return makeLogRecord(record_fields)


@pytest.mark.parametrize(
    ("json_field", "base_attribute"),
    [("level", "levelname"), ("module", "module"), ("message", "msg")],
)
def test_desired_base_attributes_are_included_in_json(json_field, base_attribute):
    value = a_string()
    record = _build_log_record(**{base_attribute: value})

    actual_json_string = JsonFormatter().format(record)
    actual = json.loads(actual_json_string)[json_field]

    assert value == actual


@pytest.mark.parametrize(
    "base_attribute",
    [
        "name",
        "args",
        "levelno",
        "pathname",
        "filename",
        "exc_info",
        "exc_text",
        "stack_info",
        "lineno",
        "funcName",
        "msecs",
        "relativeCreated",
        "thread",
        "threadName",
        "processName",
        "process",
    ],
)
def test_undesired_base_attributes_are_not_in_json(base_attribute):
    record = _build_log_record()

    actual_json_string = JsonFormatter().format(record)
    actual = json.loads(actual_json_string)

    assert base_attribute not in actual


def test_timestamp_is_included_in_json():
    record = _build_log_record(created=1607965513.358049)
    actual_json_string = JsonFormatter().format(record)
    actual = json.loads(actual_json_string)["time"]

    expected = "2020-12-14T17:05:13.358049"

    assert actual == expected


def test_extra_field_is_included_in_json():
    record = _build_log_record(extra={"fruit": "mango"})

    actual_json_string = JsonFormatter().format(record)
    actual = json.loads(actual_json_string)["fruit"]

    expected = "mango"

    assert actual == expected


def test_extra_fields_is_included_in_json():
    record = _build_log_record(extra={"fruit": "mango", "colour": "red"})

    actual_json_string = JsonFormatter().format(record)
    actual_json = json.loads(actual_json_string)

    actual_fruit = actual_json["fruit"]
    actual_colour = actual_json["colour"]

    expected_fruit = "mango"
    expected_colour = "red"

    assert actual_fruit == expected_fruit and actual_colour == expected_colour
