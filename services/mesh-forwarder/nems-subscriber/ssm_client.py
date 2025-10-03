import boto3
import os


def send_to_smm_with_client(new_subscription_id, current_subscription_id, client, nhs_environment):
    if new_subscription_id != current_subscription_id:
        print(f'Updating nems subscription id in smm to be {new_subscription_id}')
        response = client.put_parameter(
            Name=f'/repo/{nhs_environment}/user-input/external/nems-subscription-id',
            Value=new_subscription_id,
            Type='String',
            Overwrite=True
        )
        print(response)
        print(f'Successfully saved new nems subscription id {new_subscription_id} to ssm')
    else:
        print(f'Not updating nems subscription id in ssm because new id {new_subscription_id} is identical to current '
              f'ssm id {current_subscription_id}')


def send_to_ssm(new_subscription_id, current_subscription_id):
    client = boto3.client('ssm', region_name='eu-west-2')
    nhs_environment = os.getenv('NHS_ENVIRONMENT')
    send_to_smm_with_client(new_subscription_id, current_subscription_id, client, nhs_environment)
