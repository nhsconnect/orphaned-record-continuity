# prm-mesh-aws-forwarder

Generic AWS MESH forwarder, capable of reading messages from a MESH inbox and writing them to AWS S3 or SNS.

## Developing

### Prerequistes
In order to get started with development, you will need Python - version 3.9 or higher.

To run the tests in the same container image used in the CI pipeline, you will need:
- [Docker](https://www.docker.com/get-started) - version 3.1.0 or higher
- Python 3.9. Use [pyenv](https://github.com/pyenv/pyenv) to easily switch Python versions.
- [Pipenv](https://pypi.org/project/pipenv/). Install by running `python -m pip install pipenv`

#### Installing the correct versions of pip and python locally

Ensure you are not within a virtual environment (run `deactivate` if you are in one)

1. Run `pyenv install 3.9.4`
2. Follow step 3 from [here](https://github.com/pyenv/pyenv#basic-github-checkout )
3. Run `pyenv global 3.9.4`
4. For the following steps open another terminal.
5. Run `python -m pip install pipenv` to install pipenv using the updated python environment.
6. Run `python -m pip install -U "pip>=21.1`
   - `pyenv global` should output the specific python version specified rather than `system`.
   - Both `python --version` and `pip --version` should point to the versions you have specified.
   - `ls -l $(which pipenv)` should output `.../.pyenv/shims/pipenv` rather than `...Cellar...` (which is a brew install).

#### Python virtual environment

From the base directory of the project, create a python3 virtual environment by running `./tasks devenv`, then to activate it run `pipenv shell`

To deactivate the virtual environment run `deactivate`.

To remove the virtual environment and clear the cache, run `pipenv --rm && pipenv --clear`.

Run the following commands in the virtual environment:

### Scripts

### Running tests, linting, and type checking

`./tasks validate`. This should be done before commiting.


### Troubleshooting

#### Checking dependencies fails locally due to pip

If running `./tasks check-deps` fails due to an outdated version of pip, then the local python environment containing pipenv may need to be updated (using pyenv instead of brew - to better control the pip version).
Ensure you have pyenv installed (use `brew install pyenv`).
Perform the following steps:

1. Run `brew uninstall pipenv`
2. Run the steps listed under [Installing correct version of pip and python](#installing-correct-version-of-pip-and-python)
3. Now running `./tasks check-deps` should pass.

#### Python virtual environments

If you see the below notice when trying to activate the python virtual environment, run `deactivate` before trying again.

## Running

Install with `python setup.py install`.

The AWS MESH forwarder can then be started with `python -m awsmesh.entrypoint`.

Running the forwarder requires the following environment variables to be set:

| Environment variable            | Description                                                                                             | 
| ------------------------------- | ------------------------------------------------------------------------------------------------------- |
| MESH_URL                        | URL of MESH endpoint to connect to                                                                      |
| MESH_MAILBOX_SSM_PARAM_NAME     | Name of AWS SSM parameter store entry containing the name of the MESH inbox to consume messages from    |
| MESH_PASSWORD_SSM_PARAM_NAME    | Name of AWS SSM parameter store entry containing the password of the MESH inbox to consume message from |
| MESH_SHARED_KEY_SSM_PARAM_NAME  | Name of AWS SSM parameter store entry containing the MESH shared key                                    |
| MESH_CLIENT_CERT_SSM_PARAM_NAME | Name of AWS SSM parameter store entry containing the client certificate                                 |
| MESH_CLIENT_KEY_SSM_PARAM_NAME  | Name of AWS SSM parameter store entry containing the client certificate key                             |
| MESH_CA_CERT_SSM_PARAM_NAME     | Name of AWS SSM parameter store entry containing the certificate authority chain                        |
| MESSAGE_DESTINATION             | Service messages are published to (s3 or sns)                                                           |
| S3_BUCKET_NAME                  | S3 bucket to publish messages to (defined if MESSAGE_DESTINATION="s3")                                  |
| SNS_TOPIC_ARN                   | SNS topic to publish messages to (defined if MESSAGE_DESTINATION="sns")                                 |
| POLL_FREQUENCY                  | Duration in seconds between each poll of the mesh mailbox                                               |
| FORWARDER_HOME                  | Directory used to store certificates extracted from parameter store                                     |
