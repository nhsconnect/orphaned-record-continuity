from unittest.mock import Mock

from create_subscription_if_not_exists import create_subscription_if_not_exists
from config import Config


def test_that_when_subscription_does_not_exist_it_is_created():
    config = Config(repo_asid="A1", ods_code="ods", nems_url="http://host:8080", mesh_mailbox_id="mesh", nems_subscription_id="sub", nhs_env="testing", nems_asid="ASID", issuer="testing.pre-prod")
    reader = Mock(return_value=404)
    creator = Mock(return_value='new_sub_id')

    result = create_subscription_if_not_exists(reader, creator, config)

    creator.assert_called_once_with(config)
    assert result == 'new_sub_id'


def test_that_when_no_subscription_id_present_it_is_created():
    config = Config(repo_asid="A1", ods_code="ods", nems_url="http://host:8080", mesh_mailbox_id="mesh", nhs_env="testing", nems_asid="ASID", issuer="testing.pre-prod")
    reader = Mock()
    creator = Mock(return_value='new_sub_id')

    result = create_subscription_if_not_exists(reader, creator, config)

    reader.assert_not_called()
    creator.assert_called_once_with(config)
    assert result == 'new_sub_id'


def test_that_when_subscription_does_exist_it_is_not_recreated():
    config = Config(repo_asid="A1", ods_code="ods", nems_url="http://host:8080", mesh_mailbox_id="mesh", nems_subscription_id="sub", nhs_env="testing", nems_asid="ASID", issuer="testing.pre-prod")

    reader = Mock(return_value=200)
    creator = Mock()

    result = create_subscription_if_not_exists(reader, creator, config)

    creator.assert_not_called()
    assert result == config.nems_subscription_id





