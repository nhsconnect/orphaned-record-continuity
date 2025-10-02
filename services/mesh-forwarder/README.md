# prm-deductions-mesh-forwarder
A service to deploy MESH forwarder infrastructure in scope of repository team continuity service.

MESH forwarder is responsible reading messages from a MESH inbox and passing them onto the downstream components for further processing

## Prerequisites

Follow the links to download
- [Docker](https://docs.docker.com/install/)
- [kudulab/dojo](https://github.com/kudulab/dojo#installation)


### Manual query and send interactions

Please refer to [MESH summary page on confluence](https://gpitbjss.atlassian.net/wiki/spaces/TW/pages/11604623415/MESH) and child pages.

### AWS helpers

This repository imports shared AWS helpers from [prm-deductions-support-infra](https://github.com/nhsconnect/prm-deductions-support-infra/).
They can be found `utils` directory after running any task from `tasks` file.


## Directories

| Directory         | Description                                             |
| :---------------- | :------------------------------------------------------ |
| /gocd             | Contains the GoCD pipeline files                        |
| /terraform        | Terraform to deploy component as a Fargate task in AWS  |
| /utils            | Contains aws-helpers                                    |

