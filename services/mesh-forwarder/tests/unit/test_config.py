from unittest import mock

from awsmesh.config import ForwarderConfig


def test_read_config_from_environment():
    environment = {
        "MESH_URL": "nice-mesh.biz",
        "MESH_MAILBOX_SSM_PARAM_NAME": "/params/mesh/mailbox",
        "MESH_PASSWORD_SSM_PARAM_NAME": "/params/mesh/password",
        "MESH_SHARED_KEY_SSM_PARAM_NAME": "/params/mesh/shared-key",
        "MESH_CLIENT_CERT_SSM_PARAM_NAME": "/params/mesh/client-cert",
        "MESH_CLIENT_KEY_SSM_PARAM_NAME": "/params/mesh/client-key",
        "MESH_CA_CERT_SSM_PARAM_NAME": "/params/mesh/ca-cert",
        "S3_BUCKET_NAME": "mesh-data-bucket",
        "POLL_FREQUENCY": "60",
        "FORWARDER_HOME": "/home/mesh-forwarder",
        "ENDPOINT_URL": "https://an.endpoint:3000",
        "SSM_ENDPOINT_URL": "https://an.endpoint:3001",
        "DISABLE_MESSAGE_HEADER_VALIDATION": "true",
    }

    expected_config = ForwarderConfig(
        mesh_url="nice-mesh.biz",
        mesh_mailbox_ssm_param_name="/params/mesh/mailbox",
        mesh_password_ssm_param_name="/params/mesh/password",
        mesh_shared_key_ssm_param_name="/params/mesh/shared-key",
        mesh_client_cert_ssm_param_name="/params/mesh/client-cert",
        mesh_client_key_ssm_param_name="/params/mesh/client-key",
        mesh_ca_cert_ssm_param_name="/params/mesh/ca-cert",
        s3_bucket_name="mesh-data-bucket",
        poll_frequency="60",
        forwarder_home="/home/mesh-forwarder",
        endpoint_url="https://an.endpoint:3000",
        ssm_endpoint_url="https://an.endpoint:3001",
        disable_message_header_validation=True,
    )

    actual_config = ForwarderConfig.from_environment_variables(environment)

    assert actual_config == expected_config


def test_read_config_from_environment_uses_default_when_no_var_set():
    environment = {
        "MESH_URL": "nice-mesh.biz",
        "MESH_MAILBOX_SSM_PARAM_NAME": "/params/mesh/mailbox",
        "MESH_PASSWORD_SSM_PARAM_NAME": "/params/mesh/password",
        "MESH_SHARED_KEY_SSM_PARAM_NAME": "/params/mesh/shared-key",
        "MESH_CLIENT_CERT_SSM_PARAM_NAME": "/params/mesh/client-cert",
        "MESH_CLIENT_KEY_SSM_PARAM_NAME": "/params/mesh/client-key",
        "MESH_CA_CERT_SSM_PARAM_NAME": "/params/mesh/ca-cert",
        "S3_BUCKET_NAME": "mesh-data-bucket",
        "POLL_FREQUENCY": "60",
        "FORWARDER_HOME": "/home/mesh-forwarder",
    }

    expected_config = ForwarderConfig(
        mesh_url="nice-mesh.biz",
        mesh_mailbox_ssm_param_name="/params/mesh/mailbox",
        mesh_password_ssm_param_name="/params/mesh/password",
        mesh_shared_key_ssm_param_name="/params/mesh/shared-key",
        mesh_client_cert_ssm_param_name="/params/mesh/client-cert",
        mesh_client_key_ssm_param_name="/params/mesh/client-key",
        mesh_ca_cert_ssm_param_name="/params/mesh/ca-cert",
        s3_bucket_name="mesh-data-bucket",
        poll_frequency="60",
        forwarder_home="/home/mesh-forwarder",
        endpoint_url=None,
        ssm_endpoint_url=None,
        disable_message_header_validation=False,
    )

    actual_config = ForwarderConfig.from_environment_variables(environment)

    assert actual_config == expected_config


@mock.patch("sys.exit")
def test_read_config_from_environment_calls_exit_when_missing_variable(mock_exit):
    ForwarderConfig.from_environment_variables({})
    mock_exit.assert_called_with(1)


# flake8: noqa: E712 E501
def test_a_pythonesque_falsey_string_value_for_disable_validation_setting_is_interpreted_correctly():
    environment = _dummy_env_data_for_required_variables()
    environment["DISABLE_MESSAGE_HEADER_VALIDATION"] = "false"

    actual_config = ForwarderConfig.from_environment_variables(environment)

    assert actual_config.disable_message_header_validation == False


# flake8: noqa: E712
def test_an_empty_string_value_for_disable_validation_setting_is_interpreted_as_false():
    environment = _dummy_env_data_for_required_variables()
    environment["DISABLE_MESSAGE_HEADER_VALIDATION"] = ""

    actual_config = ForwarderConfig.from_environment_variables(environment)

    assert actual_config.disable_message_header_validation == False


# flake8: noqa: E712
def test_an_invalid_string_value_for_disable_validation_setting_is_interpreted_as_false():
    environment = _dummy_env_data_for_required_variables()
    environment["DISABLE_MESSAGE_HEADER_VALIDATION"] = "tgxh"

    actual_config = ForwarderConfig.from_environment_variables(environment)

    assert actual_config.disable_message_header_validation == False


def _dummy_env_data_for_required_variables():
    return {
        "MESH_URL": "nice-mesh.biz",
        "MESH_MAILBOX_SSM_PARAM_NAME": "/params/mesh/mailbox",
        "MESH_PASSWORD_SSM_PARAM_NAME": "/params/mesh/password",
        "MESH_SHARED_KEY_SSM_PARAM_NAME": "/params/mesh/shared-key",
        "MESH_CLIENT_CERT_SSM_PARAM_NAME": "/params/mesh/client-cert",
        "MESH_CLIENT_KEY_SSM_PARAM_NAME": "/params/mesh/client-key",
        "MESH_CA_CERT_SSM_PARAM_NAME": "/params/mesh/ca-cert",
        "S3_BUCKET_NAME": "mesh-data-bucket",
        "POLL_FREQUENCY": "60",
        "FORWARDER_HOME": "/home/mesh-forwarder",
    }
