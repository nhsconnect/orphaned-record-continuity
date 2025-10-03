from config import read_subscribe_config_from_env

from create_headers import create_headers


def print_read_subscription(config):
    print(f'Printing Request Headers: {create_headers(config, "Get")}')


if __name__ == "__main__":
    print_read_subscription(read_subscribe_config_from_env())