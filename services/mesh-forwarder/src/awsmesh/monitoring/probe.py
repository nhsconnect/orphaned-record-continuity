from logging import Logger, getLogger

from awsmesh.monitoring.event.count import CountMessagesEvent
from awsmesh.monitoring.event.forward import ForwardMessageEvent
from awsmesh.monitoring.event.poll import PollInboxEvent
from awsmesh.monitoring.output import LoggingOutput

logger = getLogger(__name__)


class LoggingProbe:
    def __init__(self, log: Logger = logger):
        self._output = LoggingOutput(log)

    def new_count_messages_event(self) -> CountMessagesEvent:
        return CountMessagesEvent(self._output)

    def new_forward_message_event(self) -> ForwardMessageEvent:
        return ForwardMessageEvent(self._output)

    def new_poll_inbox_event(self) -> PollInboxEvent:
        return PollInboxEvent(self._output)
