from threading import Thread

from awsmesh.entrypoint import build_forwarder_from_environment_variables
from tests.builders.common import a_string

SENDING_MESH_MAILBOX = "e2e-sns-test-mailbox-sender"
RECEIVING_MESH_MAILBOX = "e2e-sns-test-mailbox-receiver"

AWS_REGION = "us-west-1"

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


def _build_sns_forwarder(config, sns_topic_arn):
    env_config = {
        **config,
        "MESSAGE_DESTINATION": "sns",
        "SNS_TOPIC_ARN": sns_topic_arn,
    }

    forwarder = build_forwarder_from_environment_variables(env_config)
    return ThreadedForwarder(forwarder)


class SnsTopic:
    def __init__(self, aws_client, arn) -> None:
        self.arn = arn
        self._aws_client = aws_client

    def subscribe(self, queue):
        self._aws_client.subscribe(
            TopicArn=self.arn,
            Protocol="sqs",
            Endpoint=queue.arn,
            Attributes={"RawMessageDelivery": "true"},
        )


class SnsForTesting:
    def __init__(self, e2e_test_context) -> None:
        self._aws_client = e2e_test_context.build_aws_client("sns")

    def create_topic(self, sns_topic_name):
        self._aws_client.create_topic(Name=sns_topic_name)
        topic_list_response = self._aws_client.list_topics()
        topic_arn = topic_list_response["Topics"][0]["TopicArn"]
        return SnsTopic(self._aws_client, topic_arn)


class SqsQueue:
    def __init__(self, aws_client, name, url, arn) -> None:
        self.name = name
        self.url = url
        self.arn = arn
        self._aws_client = aws_client

    def receive_messages(self):
        messages = self._aws_client.receive_message(
            QueueUrl=self.url, WaitTimeSeconds=10, MessageAttributeNames=["All"]
        )["Messages"]
        return messages


class SqsForTesting:
    def __init__(self, e2e_test_context) -> None:
        self._aws_client = e2e_test_context.build_aws_client("sqs")
        self.sqs_arn = e2e_test_context.get_service_arn("sqs")

    @property
    def aws_client(self):
        return self._aws_client

    def create_queue(self, queue_name):
        queue_url = self._aws_client.create_queue(QueueName=queue_name)["QueueUrl"]
        queue_arn = f"{self.sqs_arn}:{queue_name}"
        return SqsQueue(self._aws_client, queue_name, queue_url, queue_arn)


def test_mesh_inbox_sns_forwarder(e2e_test_context):
    mesh = e2e_test_context.build_mesh_client(SENDING_MESH_MAILBOX)

    e2e_test_context.set_fake_aws_environment_vars()
    e2e_test_context.populate_ssm_parameters(
        test_name="test-sns", receiving_mailbox_name=RECEIVING_MESH_MAILBOX
    )
    sns = SnsForTesting(e2e_test_context)
    sqs = SqsForTesting(e2e_test_context)

    queue = sqs.create_queue("test-queue")
    topic = sns.create_topic("test-topic")

    topic.subscribe(queue)

    message_contents = a_string()

    forwarder = _build_sns_forwarder(
        sns_topic_arn=topic.arn,
        config=e2e_test_context.build_forwarder_config(test_name="test-sns"),
    )

    try:
        forwarder.start()
        mesh.send_message(RECEIVING_MESH_MAILBOX, message_contents.encode("utf-8"))

        message = queue.receive_messages()[0]

        assert message["Body"] == message_contents
        assert "meshMessageId" in message["MessageAttributes"]
    finally:
        forwarder.stop()
        e2e_test_context.unset_fake_aws_environment_vars()
