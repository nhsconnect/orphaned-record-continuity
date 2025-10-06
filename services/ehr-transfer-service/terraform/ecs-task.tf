locals {
  task_role_arn                = aws_iam_role.component-ecs-role.arn
  task_execution_role          = "arn:aws:iam::${data.aws_caller_identity.current.account_id}:role/${aws_iam_role.component-ecs-role.name}"
  task_ecr_url                 = "${data.aws_caller_identity.current.account_id}.dkr.ecr.${var.region}.amazonaws.com"
  task_log_group               = "/nhs/deductions/${var.environment}-${data.aws_caller_identity.current.account_id}/${var.component_name}"
  environment_variables        = [
    { name = "EHR_TRANSFER_SERVICE_MHS_QUEUE_URL_1", value = data.aws_ssm_parameter.openwire_endpoint_0.value },
    { name = "EHR_TRANSFER_SERVICE_MHS_QUEUE_URL_2", value = data.aws_ssm_parameter.openwire_endpoint_1.value },
    {
      name  = "EHR_TRANSFER_SERVICE_EHR_REPO_URL",
      value = "https://ehr-repo.${data.aws_ssm_parameter.env_domain_name.value}"
    },
    { name = "EHR_TRANSFER_SERVICE_LOG_LEVEL", value = var.log_level },
    { name = "EHR_TRANSFER_SERVICE_LOG_LEVEL", value = var.log_level },
    { name = "REPO_INCOMING_QUEUE_NAME", value = aws_sqs_queue.repo_incoming.name },
    { name = "SMALL_EHR_QUEUE_NAME", value = aws_sqs_queue.small_ehr.name },
    { name = "SMALL_EHR_OBSERVABILITY_QUEUE_NAME", value = aws_sqs_queue.small_ehr_observability.name },
    { name = "LARGE_EHR_QUEUE_NAME", value = aws_sqs_queue.large_ehr.name },
    { name = "NEGATIVE_ACKS_QUEUE_NAME", value = aws_sqs_queue.negative_acks.name },
    { name = "LARGE_MESSAGE_FRAGMENTS_QUEUE_NAME", value = aws_sqs_queue.large_message_fragments.name },
    { name = "EHR_COMPLETE_QUEUE_NAME", value = aws_sqs_queue.ehr_complete.name },
    { name = "TRANSFER_TRACKER_DB_NAME", value = data.aws_ssm_parameter.transfer_tracker_db_name.value },
    { name = "NHS_ENVIRONMENT", value = var.environment },
    {
      name  = "EHR_TRANSFER_SERVICE_GP2GP_MESSENGER_URL",
      value = "https://gp2gp-messenger.${data.aws_ssm_parameter.env_domain_name.value}"
    },
    { name = "REPOSITORY_ODS_CODE", value = data.aws_ssm_parameter.repository_ods_code.value },
    { name = "REPOSITORY_ASID", value = data.aws_ssm_parameter.repository_asid.value },
    { name = "SQS_LARGE_MESSAGE_BUCKET_NAME", value = aws_s3_bucket.sqs_large_message_bucket.bucket },
    { name = "SMALL_EHR_TOPIC_ARN", value = aws_sns_topic.small_ehr.arn },
    { name = "LARGE_EHR_TOPIC_ARN", value = aws_sns_topic.large_ehr.arn },
    { name = "LARGE_MESSAGE_FRAGMENTS_TOPIC_ARN", value = aws_sns_topic.large_message_fragments.arn },
    { name = "PARSING_DLQ_TOPIC_ARN", value = aws_sns_topic.parsing_dlq.arn },
    { name = "POSITIVE_ACKS_TOPIC_ARN", value = aws_sns_topic.positive_acks.arn },
    { name = "NEGATIVE_ACKS_TOPIC_ARN", value = aws_sns_topic.negative_acks.arn },
    { name = "EHR_IN_UNHANDLED_TOPIC_ARN", value = aws_sns_topic.ehr_in_unhandled.arn },
    { name = "EHR_COMPLETE_TOPIC_ARN", value = aws_sns_topic.ehr_complete.arn },
    { name = "TRANSFER_COMPLETE_TOPIC_ARN", value = aws_sns_topic.transfer_complete.arn },
    { name = "SPLUNK_UPLOADER_TOPIC_ARN", value = aws_sns_topic.splunk_uploader.arn },
    { name = "TIMEOUT_DURATION_IN_SECONDS", value = var.timeout_in_seconds },
    { name = "TIMEOUT_SCHEDULER_FIXED_RATE_IN_SECONDS", value = var.timeout_scheduler_fixed_rate_in_seconds },
    { name = "PROCESSING_PERIOD_MILLISECONDS", value = var.processing_period_milliseconds },
    { name = "EHR_TRANSFER_FINALISED_POLL_PERIOD_MILLISECONDS", value = var.ehr_transfer_finalised_poll_period_milliseconds },
    { name = "INBOUND_TIMEOUT_SECONDS", value = var.inbound_timeout_seconds }
  ]
  secret_environment_variables = [
    { name = "EHR_TRANSFER_SERVICE_MHS_QUEUE_USERNAME", valueFrom = data.aws_ssm_parameter.amq-username.arn },
    { name = "EHR_TRANSFER_SERVICE_MHS_QUEUE_PASSWORD", valueFrom = data.aws_ssm_parameter.amq-password.arn },
    { name      = "EHR_TRANSFER_SERVICE_AUTHORIZATION_KEYS_FOR_EHR_REPO",
      valueFrom = data.aws_ssm_parameter.ehr_transfer_service_authorization_keys_for_ehr_repo.arn
    },
    { name      = "EHR_TRANSFER_SERVICE_AUTHORIZATION_KEYS_FOR_GP2GP_MESSENGER",
      valueFrom = data.aws_ssm_parameter.ehr_transfer_service_authorization_keys_for_gp2gp_messenger.arn
    }
  ]
}

resource "aws_ecs_task_definition" "task" {
  family                   = var.component_name
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = var.task_cpu
  memory                   = var.task_memory
  execution_role_arn       = local.task_execution_role
  task_role_arn            = local.task_role_arn

  container_definitions = templatefile("${path.module}/templates/ecs-task-def.tmpl", {
    container_name         = "${var.component_name}-container",
    ecr_url                = local.task_ecr_url,
    image_name             = "deductions/ehr-transfer-service",
    image_tag              = var.task_image_tag,
    cpu                    = var.task_cpu,
    memory                 = var.task_memory,
    log_region             = var.region,
    log_group              = local.task_log_group
    environment_variables  = jsonencode(local.environment_variables),
    secrets                = jsonencode(local.secret_environment_variables)
  })

  tags = {
    Environment = var.environment
    CreatedBy   = var.repo_name
  }
}
