import time
import jwt


def create_headers(config, method):
    token = generate_auth_token(config)
    headers = {
        'Accept': 'application/fhir+xml;charset=utf-8',
        'fromASID': config.repo_asid,
        'toASID': config.nems_asid,
        'Authorization': f'Bearer {token}',
        'InteractionID': f'urn:nhs:names:services:clinicals-sync:SubscriptionsApi{method}',
        'X-Spine-SSLProtocol': 'TLSv1.2',
        'X-Spine-SSLCipher': 'ECDHE-RSA-AES256-GCM-SHA384'
    }
    print(f"Headers created for request: fromASID - {config.repo_asid}, toASID - {config.nems_asid}, "
          f"InteractionID - {headers.get('InteractionID')}", )
    return headers


def generate_auth_token(config):
    now = int(time.time())
    five_minutes_from_now = now + 300
    jwt_payload_data = {
        'iss': config.issuer,
        'sub': f'https://fhir.nhs.uk/Id/accredited-system|{config.repo_asid}',
        'aud': config.nems_url,
        'exp': five_minutes_from_now,
        'iat': now,
        'reason_for_request': 'secondaryuses',
        'scope': 'patient/Subscription.write',
        'requesting_system': f'https://fhir.nhs.uk/Id/accredited-system|{config.repo_asid}',
        'requesting_organization': f"https://fhir.nhs.uk/Id/ods-organization-code|{config.ods_code}",
    }

    token = jwt.encode(
        payload=jwt_payload_data,
        key=None,
        algorithm=None
    )

    print('Created new auth token')
    return token
