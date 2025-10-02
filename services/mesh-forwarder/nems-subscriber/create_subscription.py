import requests

from config import read_subscribe_config_from_env

from create_headers import create_headers


def create_subscription(config):
    print(f'Requesting Create Subscription: {config.nems_url}')

    subscribe_payload = create_payload(config)

    r = requests.post(
        config.nems_url,
        data=subscribe_payload,
        headers=create_headers(config, 'Post'),
        cert=(f"../certs/{config.nhs_env}/nems-client.crt", f"../certs/{config.nhs_env}/nems-client.key"),
        verify=f"../certs/{config.nhs_env}/nems-ca-certs.crt"
    )

    print('Create Subscription Response', r.status_code, r.headers, r.content)

    if r.status_code == 201:
        subscription_id = extract_subscription_id_from_headers(r.headers)
        print(f"Successfully created new nems subscription: subscription id - {subscription_id}")
        return subscription_id
    else:
        raise Exception(f"Error creating subscription: Status Code {r.status_code}. Error {r.content.decode()}")


def create_payload(config):
    subscribe_payload = '<Subscription xmlns="http://hl7.org/fhir">' + \
                        '<meta>' + \
                        '	<profile value="https://fhir.nhs.uk/STU3/StructureDefinition/EMS-Subscription-1"/>' + \
                        '</meta>' + \
                        '<status value="requested"/>' + \
                        '<contact>' + \
                        '	<system value="url"/>' + \
                        f'	<value value="https://directory.spineservices.nhs.uk/STU3/Organization/{config.ods_code.upper()}"/>' + \
                        '	<use value="work"/>' + \
                        '</contact>' + \
                        '<reason value="To facilate GP2GP transfer of EHR for suspended patients from their previous practise"/>' + \
                        f'<criteria value="/Bundle?type=message&amp;MessageHeader.event=pds-change-of-gp-1' \
                        f'&amp;subscriptionRuleType=COUNTRYCODE&amp;Organization.identifier=E92000001" />' + \
                        '<channel>' + \
                        '	<type value="message"/>' + \
                        f'	<endpoint value="{config.mesh_mailbox_id}"/>' + \
                        '</channel>' + \
                        '</Subscription>'

    print('Create subscription payload', subscribe_payload)
    return subscribe_payload


def extract_subscription_id_from_headers(headers):
    if 'Location' in headers:
        location = headers['Location']
        return location.split('STU3/Subscription/')[1]
    else:
        raise Exception('Unable to extract location from message headers')


if __name__ == "__main__":
    create_subscription(read_subscribe_config_from_env())
