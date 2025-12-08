import logging
from dataclasses import dataclass
from threading import Event
from typing import Optional

import mesh_client

from awsmesh.forwarder import MeshToAwsForwarder, RetryableException
from awsmesh.mesh import MeshInbox
from awsmesh.message_destination_resolver import MessageDestinationConfig, resolve_message_uploader
from awsmesh.monitoring.probe import LoggingProbe

logger = logging.getLogger(__name__)


@dataclass
class MeshConfig:
    url: str
    mailbox: str
    password: str
    shared_key: bytes
    client_cert_path: str
    client_key_path: str
    ca_cert_path: str


class MeshToAwsForwarderService:
    def __init__(
        self,
        forwarder: MeshToAwsForwarder,
        poll_frequency_sec: int,
        exit_event: Optional[Event] = None,
    ):
        self._forwarder = forwarder
        self._exit_event = exit_event or Event()
        self._poll_frequency_sec = poll_frequency_sec

    def start(self):
        logger.info("Started forwarder service")
        while not self._exit_event.is_set():
            try:
                self._forwarder.forward_messages()

                if self._forwarder.is_mailbox_empty():
                    self._exit_event.wait(self._poll_frequency_sec)
            except RetryableException:
                self._exit_event.wait(self._poll_frequency_sec)
        logger.info("Exiting forwarder service")

    def stop(self):
        logger.info("Received request to stop")
        self._exit_event.set()


def build_forwarder_service(
    mesh_config: MeshConfig,
    message_destination_config: MessageDestinationConfig,
    poll_frequency_sec: int,
    disable_message_header_validation: bool,
) -> MeshToAwsForwarderService:
    uploader = resolve_message_uploader(message_destination_config)

    mesh = mesh_client.MeshClient(
        mesh_config.url,
        mesh_config.mailbox,
        mesh_config.password,
        shared_key=mesh_config.shared_key,
        cert=(mesh_config.client_cert_path, mesh_config.client_key_path),
        verify=mesh_config.ca_cert_path,
    )
    inbox = MeshInbox(mesh)
    forwarder = MeshToAwsForwarder(
        inbox, uploader, LoggingProbe(), disable_message_header_validation
    )
    return MeshToAwsForwarderService(forwarder, poll_frequency_sec)
