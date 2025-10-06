data "aws_caller_identity" "current" {}

data "aws_ssm_parameter" "deductions_private_private_subnets" {
  name = "/repo/${var.environment}/output/prm-deductions-infra/deductions-private-private-subnets"
}

data "aws_ssm_parameter" "deductions_private_vpc_id" {
  name = "/repo/${var.environment}/output/prm-deductions-infra/private-vpc-id"
}

data "aws_ssm_parameter" "deductions_core_vpc_id" {
  name = "/repo/${var.environment}/output/prm-deductions-infra/deductions-core-vpc-id"
}

data "aws_ssm_parameter" "amq-username" {
  name = "/repo/${var.environment}/user-input/mq-app-username"
}

data "aws_ssm_parameter" "amq-password" {
  name = "/repo/${var.environment}/user-input/mq-app-password"
}

data "aws_ssm_parameter" "openwire_endpoint_0" {
  name = "/repo/${var.environment}/output/prm-deductions-infra/openwire-endpoint-0"
}

data "aws_ssm_parameter" "openwire_endpoint_1" {
  name = "/repo/${var.environment}/output/prm-deductions-infra/openwire-endpoint-1"
}

data "aws_ssm_parameter" "ehr_transfer_service_authorization_keys_for_ehr_repo" {
  name = "/repo/${var.environment}/user-input/api-keys/ehr-repo/ehr-transfer-service"
}

data "aws_ssm_parameter" "ehr_transfer_service_authorization_keys_for_gp2gp_messenger" {
  name = "/repo/${var.environment}/user-input/api-keys/gp2gp-messenger/ehr-transfer-service"
}

data "aws_ssm_parameter" "repository_ods_code" {
  name = "/repo/${var.environment}/user-input/external/repository-ods-code"
}

data "aws_ssm_parameter" "repository_asid" {
  name = "/repo/${var.environment}/user-input/external/repository-asid"
}

data "aws_ssm_parameter" "repo_incoming_topic_arn" {
  name = "/repo/${var.environment}/output/suspension-service/repo-incoming-topic-arn"
}

data "aws_ssm_parameter" "repo_incoming_kms_key" {
  name = "/repo/${var.environment}/output/suspension-service/repo-incoming-kms-key"
}

data "aws_ssm_parameter" "env_domain_name" {
  name = "/repo/${var.environment}/output/prm-deductions-infra/environment-domain-name"
}

data "aws_ssm_parameter" "transfer_tracker_db_name" {
  name = "/repo/${var.environment}/output/prm-deductions-infra/ehr-transfer-tracker-db-name"
}