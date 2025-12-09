import requests
from create_headers import create_headers
from config import read_subscribe_config_from_env


def delete_subscription(config):
    request_url = f"{config.nems_url}/{config.nems_subscription_id}"
    print(f'Requesting Delete Subscription: {request_url}')

    r = requests.delete(
        request_url,
        headers=create_headers(config, 'Delete'),
        cert=(f"../certs/{config.nhs_env}/nems-client.crt", f"../certs/{config.nhs_env}/nems-client.key"),
        verify=f"../certs/{config.nhs_env}/nems-ca-certs.crt"
    )

    print('Delete Subscription Response', r.status_code, r.headers, r.content)
    return r.status_code


if __name__ == "__main__":
    delete_subscription(read_subscribe_config_from_env())
