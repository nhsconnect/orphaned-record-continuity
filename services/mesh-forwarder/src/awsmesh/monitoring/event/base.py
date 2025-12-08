from awsmesh.mesh import MeshClientNetworkError
from awsmesh.monitoring.error import MESH_CLIENT_NETWORK_ERROR, UPLOADER_ERROR
from awsmesh.uploader import UploaderError


class BaseForwarderEvent:
    def __init__(self, output, event_name):
        self._event_name = event_name
        self._fields = {}
        self._output = output
        self._level = "info"

    def record_uploader_error(self, exception: UploaderError):
        self._fields["error"] = UPLOADER_ERROR
        self._fields["errorMessage"] = str(exception)

    def record_mesh_client_network_error(self, exception: MeshClientNetworkError):
        self._fields["error"] = MESH_CLIENT_NETWORK_ERROR
        self._fields["errorMessage"] = str(exception)

    def finish(self):
        self._output.log_event(self._event_name, self._fields, self._level)
