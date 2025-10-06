# Deductions GP2GP messenger

This is an implementation of a component to handle the sending of the GP2GP message set used to transfer a patient's Electronic Health Record between GP Practices.
It uses the GP2GP message format to transfer orphaned and stranded records out of a secure NHS repository.

This component will communicate with the Message Handler Service (MHS) [GitHub nhsconnect/integration-adaptors](https://github.com/nhsconnect/integration-adaptors) and other components being developed by the Orphaned and Stranded Record programme.

The initial version will send health records that are encoded in the HL7 format. A subsequent enhancement will be access to the components of the Health Record so that other services can use this component to send and receive Health Records with the need to implement the encoding and fragmentation strategies of the [GP2GP v2.2a](https://data.developer.nhs.uk/dms/mim/6.3.01/Domains/GP2GP/Document%20files/GP2GP%20IM.htm) message specification.

## Prerequisites

- [Node](https://nodejs.org/en/download/package-manager/#nvm) - version 14.x
- [Docker](https://docs.docker.com/install/)
- [kudulab/dojo](https://github.com/kudulab/dojo#installation)

### AWS helpers

This repository imports shared AWS helpers from [prm-deductions-support-infra](https://github.com/nhsconnect/prm-deductions-support-infra/).
They can be found `utils` directory after running any task from `tasks` file.

## Set up
To replicate the CI environment, we use `dojo` that allows us to work with the codebase without installing any dependencies locally.
Please see the `./tasks` file that includes all the tasks you can use to configure and run the app and the tests.

If you would like to run the app locally outside `dojo`, you need to:
1. Run `npm install` to install all node dependencies as per `package.json`.
2. Set up the env variables and/or copy them into your IDE configurations (`Run -> Edit Configurations ->Environment Variables` in IntelliJ):
```
export E2E_TEST_AUTHORIZATION_KEYS_FOR_GP2GP_MESSENGER=auth-key-2
export REPOSITORY_URI=$IMAGE_REPO_NAME   
export NHS_SERVICE=gp2gp-messenger
export SERVICE_URL=http://${NHS_SERVICE}:3000
export NHS_ENVIRONMENT=local
export GP2GP_MESSENGER_REPOSITORY_ASID=deduction-asid
export GP2GP_MESSENGER_REPOSITORY_ODS_CODE=deduction-ods
```
- Locally, the variables `GP2GP_MESSENGER_REPOSITORY_ASID`, `GP2GP_MESSENGER_REPOSITORY_ODS_CODE` can be set
  to any value
  
3. The app will use a fake MHS when `NHS_ENVIRONMENT` is set to `local` or `dev`. 
 
## Running the tests

Run the unit tests with

by entering the `dojo` container and running `./tasks _test_unit`
or on your machine with `npm run test:unit`

Run the integration tests within a Dojo container

1. Run `dojo -c Dojofile-itest` which will spin up the testing container
2. Run `./tasks _test_integration`

You can also run them with `npm run test:integration` but that will require some additional manual set-up


Run the coverage tests (unit test and integration test)

By entering the `dojo` container and running `./tasks _test_coverage`

or run `npm run test:coverage` on your machine

You don't have to enter the dojo container every time, you can also just run any task in your terminal:
For example:

`./tasks test_coverage`

`./tasks test_unit`

`./tasks dep` - to run audit

## Start the app locally

1. Run a development server with `npm run start:local`

### Swagger

The swagger documentation for the app can be found at `http://localhost:3000/swagger`. To update it, change the
`src/swagger.json` file. You can use the editor `https://editor.swagger.io/` which will validate your changes.

### Example request

```
curl -X POST "http://localhost:3000/ehr-request" -H "accept: application/json" -H "Authorization: auth-key-1" -H "Content-Type: application/json" -d "{ \"nhsNumber\": \"some-nhs-number\", \"odsCode\": \"some-ods-code\"}"
```

## Start the app in production mode

Compile the code with `npm run build`, and then start the server with `npm start`.

## Config

Ensure you have VPN connection set up to both `dev` and `test` environments:
[CLICK HERE](https://gpitbjss.atlassian.net/wiki/spaces/TW/pages/1832779966/VPN+for+Deductions+Services)

## Access to AWS from CLI

## Access to AWS

In order to get sufficient access to work with terraform or AWS CLI, please follow the instructions on this [confluence pages](https://gpitbjss.atlassian.net/wiki/spaces/TW/pages/11384160276/AWS+Accounts+and+Roles)
and [this how to?](https://gpitbjss.atlassian.net/wiki/spaces/TW/pages/11286020174/How+to+set+up+access+to+AWS+from+CLI)

As a note, this set-up is based on the README of assume-role [tool](https://github.com/remind101/assume-role)

## Assume role with elevated permissions

### Install `assume-role` locally:
`brew install remind101/formulae/assume-role`

Run the following command with the profile configured in your `~/.aws/config`:

`assume-role admin`

### Run `assume-role` with dojo:
Run the following command with the profile configured in your `~/.aws/config`:

`eval $(dojo "echo <mfa-code> | assume-role dev"`
or
`assume-role dev [here choose one of the options from your config: ci/dev/test]`

Run the following command to confirm the role was assumed correctly:

`aws sts get-caller-identity`
