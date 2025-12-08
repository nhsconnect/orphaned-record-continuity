import re
from datetime import datetime
from threading import Thread
from time import sleep

from awsmesh.entrypoint import build_forwarder_from_environment_variables
from tests.builders.common import a_string

SENDING_MESH_MAILBOX = "e2e-s3-test-mailbox-sender"
RECEIVING_MESH_MAILBOX = "e2e-s3-test-mailbox-receiver"

WAIT_60_SEC = {"Delay": 5, "MaxAttempts": 12}


class ThreadedForwarder:
    def __init__(self, forwarder):
        self._forwarder = forwarder
        self._thread = Thread(target=forwarder.start)

    def start(self):
        self._thread.start()

    def stop(self):
        self._forwarder.stop()
        self._thread.join()


def _build_s3_forwarder(config, s3_bucket):
    env_config = {
        **config,
        "MESSAGE_DESTINATION": "s3",
        "S3_BUCKET_NAME": s3_bucket,
    }

    forwarder = build_forwarder_from_environment_variables(env_config)
    return ThreadedForwarder(forwarder)


def _wait_for_object_count(bucket, expected_count):
    object_count_match = _poll_until(lambda: len(list(bucket.objects.all())) >= expected_count)
    assert object_count_match


def _poll_until(function, timeout=30, poll_freq=1):
    elapsed_seconds = 0
    started_time = datetime.now()
    condition_reached = False
    while elapsed_seconds <= timeout and not condition_reached:
        condition_reached = function()
        sleep(poll_freq)
        elapsed_seconds = (datetime.now() - started_time).seconds
    return condition_reached


def test_mesh_inbox_s3_forwarder(e2e_test_context):
    mesh = e2e_test_context.build_mesh_client(SENDING_MESH_MAILBOX)

    e2e_test_context.set_fake_aws_environment_vars()
    e2e_test_context.populate_ssm_parameters(
        test_name="test-s3", receiving_mailbox_name=RECEIVING_MESH_MAILBOX
    )

    s3 = e2e_test_context.build_aws_resource("s3")

    s3_bucket_name = "test-bucket"
    bucket = s3.Bucket(s3_bucket_name)
    bucket.create()
    s3.meta.client.get_waiter("bucket_exists").wait(Bucket=s3_bucket_name)

    file_contents = bytes(a_string(), "utf-8")
    forwarder = _build_s3_forwarder(
        e2e_test_context.build_forwarder_config("test-s3"), s3_bucket_name
    )
    try:
        forwarder.start()

        mesh.send_message(RECEIVING_MESH_MAILBOX, file_contents)
        _wait_for_object_count(bucket, expected_count=1)
        actual_object = next(iter(bucket.objects.all()))
        assert re.match(r"\d{4}/\d{2}/\d{2}/\d{20}_\d+\.dat", actual_object.key)
        actual_file_contents = actual_object.get()["Body"].read()
        assert actual_file_contents == file_contents
    finally:
        forwarder.stop()
        e2e_test_context.unset_fake_aws_environment_vars()
