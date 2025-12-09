import os, random
from base64 import b64encode

import boto3
from locust import task, between, FastHttpUser

RECORD_ETAG_NAME='recordETag'

def extract_etag(response_data):
    return response_data[RECORD_ETAG_NAME]

def random_gp_ods_code():
    return "PERF" + str(random.randint(10000, 99999))

trace_id_prefix = 'perf' + str(random.randint(1000, 9999)) + '-'

min_wait_seconds = int(os.getenv("MIN_WAIT_SECONDS", "4"))

max_wait_seconds = int(os.getenv("MAX_WAIT_SECONDS", "5"))

class PdsAdaptorUser(FastHttpUser):
    wait_time = between(min_wait_seconds, max_wait_seconds)
    connection_timeout = 10
    network_timeout = 10
    test_patient_nhs_numbers = [
        '9694180309',
        '9694180317',
        '9694180325',
        '9694180333',
        '9694180341',
        '9694180368',
        '9694180376',
        '9694180384',
        '9694180392',
        '9694180406'
    ]

    @classmethod
    def next_patient_id(cls):
        if len(cls.test_patient_nhs_numbers) == 0:
            raise SystemExit('Ran out of test patient nhs number ids, too many users spawned')
        return cls.test_patient_nhs_numbers.pop()


    def on_start(self):
        self.patient_id = PdsAdaptorUser.next_patient_id()
        self.api_key = self.get_api_key_from_ssm()["Parameter"]["Value"]


    @task
    def suspended_patient_status(self):
        self._get_suspended_patient_status(self.patient_id)


    @task
    def organisation_field_update(self):
        status_data = self._get_suspended_patient_status(self.patient_id)
        last_etag = extract_etag(status_data)

        previous_gp = random_gp_ods_code()
        data = {
            "previousGp": previous_gp,
            RECORD_ETAG_NAME: last_etag
        }
        self.client.put(f"/suspended-patient-status/{self.patient_id}", json=data, headers=self.generate_headers())


    def _get_suspended_patient_status(self, patient_id):
        response = self.client.get(f"/suspended-patient-status/{patient_id}", headers=self.generate_headers())
        return response.json()


    def generate_headers(self):
        headers = self.generate_auth_headers()
        headers['traceId'] = self.generate_trace_id()
        return headers


    def generate_trace_id(self):
        return trace_id_prefix + str(random.randint(100000, 999999))


    def generate_auth_headers(self):
        username = "performance-test"
        encoded_key = b64encode(f"{username}:{self.api_key}".encode("utf8")).decode("ascii")
        return { "Authorization" : f"Basic {encoded_key}" }

    def get_api_key_from_ssm(self):
        env = os.environ["NHS_ENVIRONMENT"]
        ssm_parameter_name = f"/repo/{env}/user-input/api-keys/pds-adaptor/performance-test"
        return boto3.client('ssm').get_parameter(Name=ssm_parameter_name, WithDecryption=True)
