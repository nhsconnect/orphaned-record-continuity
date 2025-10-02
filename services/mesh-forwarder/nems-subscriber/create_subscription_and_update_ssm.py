from ssm_client import send_to_ssm
from config import read_subscribe_config_from_env

from create_subscription import create_subscription
from read_subscription import read_subscription
from create_subscription_if_not_exists import create_subscription_if_not_exists


def ensure_subscribed(config=read_subscribe_config_from_env()):
    subscribed_nems_id = create_subscription_if_not_exists(subscription_creator=create_subscription,
                                                           subscription_reader=read_subscription, config=config)
    return subscribed_nems_id


if __name__ == "__main__":
    config = read_subscribe_config_from_env()
    new_id = ensure_subscribed(config)
    send_to_ssm(new_id, config.nems_subscription_id)
