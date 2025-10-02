from unittest.mock import MagicMock
from ssm_client import send_to_smm_with_client


def test_should_call_smm_client_if_ids_differ():
    ssm = MagicMock()
    new_id = "1"
    current_id = "2"
    nhs_env = 'dev'
    send_to_smm_with_client(new_id, current_id, ssm, nhs_env)

    ssm.put_parameter.assert_called_once_with(
        Name='/repo/dev/user-input/external/nems-subscription-id',
        Value=new_id,
        Type='String',
        Overwrite=True
    )


def test_should_not_call_smm_client_if_ids_are_the_same():
    ssm = MagicMock()
    new_id = "1"
    current_id = "1"
    nhs_env = 'dev'
    send_to_smm_with_client(new_id, current_id, ssm, nhs_env)

    ssm.put_parameter.assert_not_called()
