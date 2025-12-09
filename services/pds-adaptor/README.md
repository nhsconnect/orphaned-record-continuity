# prm-deductions-pds-adaptor

This component is a java webservice that interacts with pds fhir api.


## Prerequisites

- Java v11 LTS
- Gradle 6.8.1

### AWS helpers

This repository imports shared AWS helpers from [prm-deductions-support-infra](https://github.com/nhsconnect/prm-deductions-support-infra/).
They can be found `utils` directory after running any task from `tasks` file.


## Set up

### Running the application

```
./tasks run_local
```

### Running the tests and additional checks

Run the unit tests with:

```
./tasks test_unit
```

In your terminal with

```
./gradlew test
```

Run the integration tests with:
```
./tasks test_integration
```

Run the coverage tests with:

```
./tasks test_coverage
```

Run the dependency check tests with:

```
./tasks dep
```

To run all the checks before committing with one command
```
./tasks test_all
```

## Access to AWS

In order to get sufficient access to work with terraform or AWS CLI:

Make sure to unset the AWS variables:
```
unset AWS_ACCESS_KEY_ID
unset AWS_SECRET_ACCESS_KEY
unset AWS_SESSION_TOKEN
```

As a note, the following set-up is based on the README of assume-role [tool](https://github.com/remind101/assume-role)

Set up a profile for each role you would like to assume in `~/.aws/config`, for example:

```
[profile default]
region = eu-west-2
output = json

[profile admin]
region = eu-west-2
role_arn = <role-arn>
mfa_serial = <mfa-arn>
source_profile = default
```

The `source_profile` needs to match your profile in `~/.aws/credentials`.
```
[default]
aws_access_key_id = <your-aws-access-key-id>
aws_secret_access_key = <your-aws-secret-access-key>
```

## Assume role with elevated permissions


In order to get sufficient access to work with terraform or AWS CLI, please export secrets from the AWS Access Portal for the environment you are using

## AWS SSM Parameters Design Principles

When creating the new ssm keys, please follow the agreed convention as per the design specified below:

* all parts of the keys are lower case
* the words are separated by dashes (`kebab case`)
* `env` is optional

### Design:
Please follow this design to ensure the ssm keys are easy to maintain and navigate through:

| Type               | Design                                  | Example                                               |
| -------------------| ----------------------------------------| ------------------------------------------------------|
| **User-specified** |`/repo/<env>?/user-input/`               | `/repo/${var.environment}/user-input/db-username`     |
| **Auto-generated** |`/repo/<env>?/output/<name-of-git-repo>/`| `/repo/output/prm-deductions-base-infra/root-zone-id` |
