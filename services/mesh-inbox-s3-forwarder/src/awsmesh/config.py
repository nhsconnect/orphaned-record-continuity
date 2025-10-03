import logging
import sys
from dataclasses import MISSING, Field, dataclass, fields
from distutils.util import strtobool
from typing import Optional

logger = logging.getLogger(__name__)


def _parse_field_from_env_var(name: str, value: str, field: Field):
    if field.type == Optional[bool]:
        try:
            return strtobool(value)
        except ValueError:
            logger.warning(
                f"Invalid value '{value}' for {name}, ignoring and using default: {field.default}"
            )
            return field.default
    return value


def _read_env(field: Field, env_vars):
    env_var_name = field.name.upper()
    if env_var_name in env_vars:
        return _parse_field_from_env_var(env_var_name, env_vars[env_var_name], field)
    elif field.default != MISSING:
        return field.default
    else:
        logger.error(f"Expected environment variable {env_var_name} was not set, exiting...")
        sys.exit(1)


@dataclass
class ForwarderConfig:
    mesh_url: str
    mesh_mailbox_ssm_param_name: str
    mesh_password_ssm_param_name: str
    mesh_shared_key_ssm_param_name: str
    mesh_client_cert_ssm_param_name: str
    mesh_client_key_ssm_param_name: str
    mesh_ca_cert_ssm_param_name: str
    poll_frequency: str
    forwarder_home: str
    message_destination: str = "s3"
    s3_bucket_name: Optional[str] = None
    sns_topic_arn: Optional[str] = None
    endpoint_url: Optional[str] = None
    ssm_endpoint_url: Optional[str] = None
    disable_message_header_validation: Optional[bool] = False

    @classmethod
    def from_environment_variables(cls, env_vars):
        return cls(**{field.name: _read_env(field, env_vars) for field in fields(cls)})
