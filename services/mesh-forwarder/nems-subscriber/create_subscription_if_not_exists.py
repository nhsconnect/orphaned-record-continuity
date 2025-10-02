from config import Config


def create_subscription_if_not_exists(subscription_reader, subscription_creator, config: Config):
    if not config.nems_subscription_id:
        print('No nems subscription id in ssm so creating new subscription')
        return subscription_creator(config)

    print(f"Verifying nems subscription id {config.nems_subscription_id} is still valid subscription")
    read_result = subscription_reader(config)
    if not read_result == 200:
        print(f"nems subscription does not exist for id {config.nems_subscription_id} so "
              f"creating new nems subscription")
        return subscription_creator(config)

    print(f"Subscription already exists for nems subscription id {config.nems_subscription_id} so not"
          f"sending request to create new subscription")
    return config.nems_subscription_id
