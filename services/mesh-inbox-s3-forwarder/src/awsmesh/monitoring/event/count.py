from awsmesh.monitoring.event.base import BaseForwarderEvent

COUNT_MESSAGES_EVENT = "COUNT_MESSAGES"


class CountMessagesEvent(BaseForwarderEvent):
    def __init__(self, output):
        super().__init__(output, COUNT_MESSAGES_EVENT)

    def record_message_count(self, count: int):
        self._fields["inboxMessageCount"] = count
