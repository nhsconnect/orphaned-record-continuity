import ssl
from os import environ, path
from tempfile import TemporaryDirectory
from threading import Thread

import boto3
import mesh_client
import pytest
from cheroot.ssl.builtin import BuiltinSSLAdapter
from cheroot.wsgi import Server
from fake_mesh import FakeMeshApplication
from moto.core import ACCOUNT_ID
from moto.server import DomainDispatcherApplication, create_backend_app

from tests.builders.common import a_string


def _mesh_client_test_file(filename):
    return path.join(path.dirname(mesh_client.__file__), filename)


def _read_file(filepath):
    with open(filepath) as f:
        return f.read()


FAKE_AWS_HOST = "127.0.0.1"
FAKE_AWS_PORT = 8887

CA_CERT_PATH = _mesh_client_test_file("ca.cert.pem")
SERVER_CERT_PATH = _mesh_client_test_file("server.cert.pem")
SERVER_KEY_PATH = _mesh_client_test_file("server.key.pem")
CLIENT_CERT_PATH = _mesh_client_test_file("client.cert.pem")
CLIENT_KEY_PATH = _mesh_client_test_file("client.key.pem")

MESH_MAILBOX_SSM_PARAM = "mesh-test-sns/mailbox-name"
MESH_PASSWORD_SSM_PARAM = "mesh-test-sns/mailbox-password"
MESH_SHARED_KEY_SSM_PARAM = "mesh-test-sns/shared-key"
MESH_CLIENT_CERT_SSM_PARAM = "mesh-test-sns/client-cert"
MESH_CLIENT_KEY_SSM_PARAM = "mesh-test-sns/client-key"
MESH_CA_CERT_SSM_PARAM = "mesh-test-sns/ca-cert"

FAKE_MESH_SHARED_KEY = a_string()
FAKE_MESH_CLIENT_PASSWORD = a_string()

FAKE_MESH_HOST = "127.0.0.1"
FAKE_MESH_PORT = 8829
FAKE_MESH_URL = f"https://localhost:{FAKE_MESH_PORT}"

AWS_REGION = "us-west-1"


class ThreadedHttpd:
    def __init__(self, httpd):
        self._httpd = httpd
        self._thread = Thread(target=httpd.safe_start)

    def start(self):
        self._thread.start()

    def stop(self):
        self._httpd.stop()
        self._thread.join()


class E2ETestContext:
    def __init__(self, fowarder_home, fake_aws_endpoint):
        self._forwarder_home = fowarder_home
        self._aws_endpoint = fake_aws_endpoint

    def build_mesh_client(self, sending_mailbox_name):
        return mesh_client.MeshClient(
            FAKE_MESH_URL,
            sending_mailbox_name,
            FAKE_MESH_CLIENT_PASSWORD,
            shared_key=FAKE_MESH_SHARED_KEY.encode("utf-8"),
            cert=(CLIENT_CERT_PATH, CLIENT_KEY_PATH),
            verify=CA_CERT_PATH,
        )

    def set_fake_aws_environment_vars(self):
        environ["AWS_ACCESS_KEY_ID"] = "testing"
        environ["AWS_SECRET_ACCESS_KEY"] = "testing"
        environ["AWS_DEFAULT_REGION"] = AWS_REGION

    def unset_fake_aws_environment_vars(self):
        del environ["AWS_ACCESS_KEY_ID"]
        del environ["AWS_SECRET_ACCESS_KEY"]
        del environ["AWS_DEFAULT_REGION"]

    def build_forwarder_config(self, test_name):
        return {
            "MESH_URL": FAKE_MESH_URL,
            "MESH_MAILBOX_SSM_PARAM_NAME": f"{test_name}/{MESH_MAILBOX_SSM_PARAM}",
            "MESH_PASSWORD_SSM_PARAM_NAME": f"{test_name}/{MESH_PASSWORD_SSM_PARAM}",
            "MESH_SHARED_KEY_SSM_PARAM_NAME": f"{test_name}/{MESH_SHARED_KEY_SSM_PARAM}",
            "MESH_CLIENT_CERT_SSM_PARAM_NAME": f"{test_name}/{MESH_CLIENT_CERT_SSM_PARAM}",
            "MESH_CLIENT_KEY_SSM_PARAM_NAME": f"{test_name}/{MESH_CLIENT_KEY_SSM_PARAM}",
            "MESH_CA_CERT_SSM_PARAM_NAME": f"{test_name}/{MESH_CA_CERT_SSM_PARAM}",
            "POLL_FREQUENCY": "5",
            "FORWARDER_HOME": self._forwarder_home,
            "ENDPOINT_URL": self._aws_endpoint,
            "SSM_ENDPOINT_URL": self._aws_endpoint,
        }

    def populate_ssm_parameters(self, test_name, receiving_mailbox_name):
        ssm = self.build_aws_client("ssm")
        ssm_parameters = {
            f"{test_name}/{MESH_MAILBOX_SSM_PARAM}": receiving_mailbox_name,
            f"{test_name}/{MESH_PASSWORD_SSM_PARAM}": FAKE_MESH_CLIENT_PASSWORD,
            f"{test_name}/{MESH_SHARED_KEY_SSM_PARAM}": FAKE_MESH_SHARED_KEY,
            f"{test_name}/{MESH_CLIENT_CERT_SSM_PARAM}": _read_file(CLIENT_CERT_PATH),
            f"{test_name}/{MESH_CLIENT_KEY_SSM_PARAM}": _read_file(CLIENT_KEY_PATH),
            f"{test_name}/{MESH_CA_CERT_SSM_PARAM}": _read_file(CA_CERT_PATH),
        }
        for name, value in ssm_parameters.items():
            ssm.put_parameter(Name=name, Value=value, Type="SecureString")

    def get_service_arn(self, service_name):
        return f"arn:aws:{service_name}:{AWS_REGION}:{ACCOUNT_ID}"

    def build_aws_client(self, name):
        return boto3.client(
            service_name=name,
            endpoint_url=self._aws_endpoint,
        )

    def build_aws_resource(self, name):
        return boto3.resource(
            service_name=name,
            endpoint_url=self._aws_endpoint,
        )


@pytest.fixture(scope="session")
def e2e_test_context():
    app = DomainDispatcherApplication(create_backend_app)
    aws_server = Server((FAKE_AWS_HOST, FAKE_AWS_PORT), app)
    aws_httpd = ThreadedHttpd(aws_server)
    aws_httpd.start()

    mesh_dir = TemporaryDirectory()
    forwarder_dir = TemporaryDirectory()

    app = FakeMeshApplication(
        mesh_dir.name, FAKE_MESH_SHARED_KEY.encode("utf-8"), FAKE_MESH_CLIENT_PASSWORD
    )
    mesh_server = Server((FAKE_MESH_HOST, FAKE_MESH_PORT), app)

    server_context = ssl.create_default_context(ssl.Purpose.CLIENT_AUTH, cafile=CA_CERT_PATH)
    server_context.load_cert_chain(SERVER_CERT_PATH, SERVER_KEY_PATH)
    server_context.check_hostname = False
    server_context.verify_mode = ssl.CERT_REQUIRED

    ssl_adapter = BuiltinSSLAdapter(SERVER_CERT_PATH, SERVER_KEY_PATH, CA_CERT_PATH)
    ssl_adapter.context = server_context

    mesh_server.ssl_adapter = ssl_adapter
    mesh_httpd = ThreadedHttpd(mesh_server)
    mesh_httpd.start()
    yield E2ETestContext(
        fowarder_home=forwarder_dir.name,
        fake_aws_endpoint=f"http://{FAKE_AWS_HOST}:{FAKE_AWS_PORT}",
    )
    mesh_httpd.stop()
    aws_httpd.stop()
    mesh_dir.cleanup()
    forwarder_dir.cleanup()
