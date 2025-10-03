class SsmSecretManager:
    def __init__(self, ssm):
        self._ssm = ssm

    def get_secret(self, name):
        response = self._ssm.get_parameter(Name=name, WithDecryption=True)
        return response["Parameter"]["Value"]

    def download_secret(self, name, output_file_path):
        secret = self.get_secret(name)
        with open(output_file_path, "w") as f:
            f.write(secret)
