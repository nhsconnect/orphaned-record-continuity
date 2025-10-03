import requests

from config import read_subscribe_config_from_env
from create_headers import create_headers


def read_subscription(config):
    request_url = f"{config.nems_url}/{config.nems_subscription_id}"
    print(f'Requesting Retrieve Subscription: {request_url}')

    r = requests.get(
        request_url,
        headers=create_headers(config, 'Get'),
        cert=(f"../certs/{config.nhs_env}/nems-client.crt", f"../certs/{config.nhs_env}/nems-client.key"),
        verify=f"../certs/{config.nhs_env}/nems-ca-certs.crt"
    )
    print('Retrieve Subscription Response', r.status_code, r.headers, r.content)
    return r.status_code


if __name__ == "__main__":
    read_subscription(read_subscribe_config_from_env())
