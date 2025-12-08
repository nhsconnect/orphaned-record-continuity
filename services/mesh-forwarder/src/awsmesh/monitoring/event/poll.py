from awsmesh.monitoring.event.base import BaseForwarderEvent

POLL_INBOX_EVENT = "POLL_MESSAGE"


class PollInboxEvent(BaseForwarderEvent):
    def __init__(self, output):
        super().__init__(output, POLL_INBOX_EVENT)

    def record_message_batch_count(self, count: int):
        self._fields["batchMessageCount"] = count
