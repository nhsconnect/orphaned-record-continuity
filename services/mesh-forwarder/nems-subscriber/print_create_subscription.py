from config import read_subscribe_config_from_env

from create_headers import create_headers
from create_subscription import create_payload


def print_create_subscription(config):
    print(f'Printing Request Headers: {create_headers(config, "Post")}')
    create_payload(config)


if __name__ == "__main__":
    print_create_subscription(read_subscribe_config_from_env())