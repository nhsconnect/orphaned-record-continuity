variable "region" {
  type    = string
  default = "eu-west-2"
}

variable "repo_name" {
  type        = string
  description = "Docker repository of Mesh to S3 forwarder"
  default = "prm-deductions-mesh-forwarder"
}

variable "component_name" {
  type = string
  default = "mesh-forwarder"
}

variable "task_image_tag" {
  type        = string
  description = "Docker image tag of Mesh to S3 forwarder"
}

variable "task_cpu" {}
variable "task_memory" {}

variable "environment" {}

variable "service_desired_count" {
  type = number
  description = "Number of desired instances of the service"
  default = 1
}

variable "poll_frequency" {}

variable "log_level" {
  type = string
  default = "debug"
}

variable "mesh_url" {
  type        = string
  description = "URL of MESH service"
}

variable "mesh_mailbox_ssm_param_name" {
  type        = string
  description = "Name of SSM parameter containing MESH mailbox name"
}

variable "mesh_password_ssm_param_name" {
  type        = string
  description = "Name of SSM parameter containing MESH mailbox password"
}

variable "mesh_shared_key_ssm_param_name" {
  type        = string
  description = "Name of SSM parameter containing MESH shared key"
}

variable "mesh_client_cert_ssm_param_name" {
  type        = string
  description = "Name of SSM parameter containing MESH client certificate"
}

variable "mesh_client_key_ssm_param_name" {
  type        = string
  description = "Name of SSM parameter containing MESH client key"
}

variable "mesh_ca_cert_ssm_param_name" {
  type        = string
  description = "Name of SSM parameter containing MESH CA certificate"
}

variable "disable_message_header_validation" {
  type        = string
  description = "if true then relaxes the restrictions on MESH message headers"
}

variable "message_destination" {}

variable "cloudwatch_alarm_evaluation_periods" {}