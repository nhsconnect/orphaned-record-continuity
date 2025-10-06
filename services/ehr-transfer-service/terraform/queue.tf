locals {
  repo_incoming_queue_name = "${var.environment}-${var.component_name}-repo-incoming"
  repo_incoming_observability_queue_name = "${var.environment}-${var.component_name}-repo-incoming-observability"
  repo_incoming_audit_queue_name = "${var.environment}-${var.component_name}-repo-incoming-audit"
  negative_acks_queue_name = "${var.environment}-${var.component_name}-negative-acknowledgments"
  negative_acks_observability_queue_name = "${var.environment}-${var.component_name}-negative-acknowledgments-observability"
  small_ehr_queue_name = "${var.environment}-${var.component_name}-small-ehr"
  small_ehr_observability_queue_name = "${var.environment}-${var.component_name}-small-ehr-observability"
  large_ehr_queue_name = "${var.environment}-${var.component_name}-large-ehr"
  large_ehr_observability_queue_name = "${var.environment}-${var.component_name}-large-ehr-observability"
  large_message_fragments_queue_name = "${var.environment}-${var.component_name}-large-message-fragments"
  large_message_fragments_observability_queue_name = "${var.environment}-${var.component_name}-large-message-fragments-observability"
  positive_acks_observability_queue_name = "${var.environment}-${var.component_name}-positive-acknowledgements-observability"
  parsing_dlq_name = "${var.environment}-${var.component_name}-parsing-dlq"
  ehr_complete_queue_name = "${var.environment}-${var.component_name}-ehr-complete"
  ehr_complete_observability_queue_name = "${var.environment}-${var.component_name}-ehr-complete-observability"
  ehr_transfer_service_audit_queue_name = "${var.environment}-${var.component_name}-audit-uploader"
  ehr_transfer_service_audit_dlq = "${var.environment}-${var.component_name}-audit-dlq"
  ehr_in_unhandled_observability_queue_name = "${var.environment}-${var.component_name}-unhandled-observability"
  max_retention_period = 1209600
  thirty_minute_retention_period = 1800
}

resource "aws_sqs_queue" "repo_incoming" {
  name                       = local.repo_incoming_queue_name
  message_retention_seconds  = local.max_retention_period
  kms_master_key_id          = data.aws_ssm_parameter.repo_incoming_kms_key.value
  receive_wait_time_seconds  = 20
  visibility_timeout_seconds = 43200

  tags = {
    Name        = local.repo_incoming_queue_name
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}

resource "aws_sqs_queue" "negative_acks" {
  name                       = local.negative_acks_queue_name
  message_retention_seconds  = local.max_retention_period
  kms_master_key_id          = aws_kms_key.negative_acks.id
  receive_wait_time_seconds  = 20
  visibility_timeout_seconds = 240

  tags = {
    Name        = local.negative_acks_queue_name
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}

resource "aws_sqs_queue" "negative_acks_observability" {
  name                       = local.negative_acks_observability_queue_name
  message_retention_seconds  = local.thirty_minute_retention_period
  kms_master_key_id          = aws_kms_key.negative_acks.id
  receive_wait_time_seconds  = 20
  visibility_timeout_seconds = 240

  tags = {
    Name        = local.negative_acks_observability_queue_name
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}

resource "aws_sqs_queue" "small_ehr" {
  name                       = local.small_ehr_queue_name
  message_retention_seconds  = local.max_retention_period
  kms_master_key_id          = aws_kms_key.small_ehr.id
  receive_wait_time_seconds  = 20
  visibility_timeout_seconds = 240

  tags = {
    Name        = local.small_ehr_queue_name
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}

resource "aws_sqs_queue" "small_ehr_observability" {
  name                       = local.small_ehr_observability_queue_name
  message_retention_seconds  = local.thirty_minute_retention_period
  kms_master_key_id          = aws_kms_key.small_ehr.id
  receive_wait_time_seconds  = 20
  visibility_timeout_seconds = 240

  tags = {
    Name        = local.small_ehr_observability_queue_name
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}

resource "aws_sqs_queue" "large_ehr" {
  name                       = local.large_ehr_queue_name
  message_retention_seconds  = local.max_retention_period
  kms_master_key_id          = aws_kms_key.large_ehr.id
  receive_wait_time_seconds  = 20
  visibility_timeout_seconds = 240

  tags = {
    Name        = local.large_ehr_queue_name
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}

resource "aws_sqs_queue" "large_ehr_observability" {
  name                       = local.large_ehr_observability_queue_name
  message_retention_seconds  = local.thirty_minute_retention_period
  kms_master_key_id          = aws_kms_key.large_ehr.id
  receive_wait_time_seconds  = 20
  visibility_timeout_seconds = 240

  tags = {
    Name        = local.large_ehr_observability_queue_name
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}

resource "aws_sqs_queue" "large_message_fragments" {
  name                       = local.large_message_fragments_queue_name
  message_retention_seconds  = local.max_retention_period
  kms_master_key_id          = aws_kms_key.large_message_fragments.id
  receive_wait_time_seconds  = 20
  visibility_timeout_seconds = 240

  tags = {
    Name        = local.large_message_fragments_queue_name
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}

resource "aws_sqs_queue" "large_message_fragments_observability" {
  name                       = local.large_message_fragments_observability_queue_name
  message_retention_seconds  = local.thirty_minute_retention_period
  kms_master_key_id          = aws_kms_key.large_message_fragments.id
  receive_wait_time_seconds  = 20
  visibility_timeout_seconds = 240

  tags = {
    Name        = local.large_message_fragments_observability_queue_name
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}

resource "aws_sqs_queue" "positive_acks_observability" {
  name                       = local.positive_acks_observability_queue_name
  message_retention_seconds  = local.thirty_minute_retention_period
  kms_master_key_id          = aws_kms_key.positive_acks.id
  receive_wait_time_seconds  = 20
  visibility_timeout_seconds = 240

  tags = {
    Name        = local.positive_acks_observability_queue_name
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}

resource "aws_sqs_queue" "parsing_dlq" {
  name                       = local.parsing_dlq_name
  message_retention_seconds  = local.max_retention_period
  kms_master_key_id          = aws_kms_key.parsing_dlq.id
  receive_wait_time_seconds  = 20
  visibility_timeout_seconds = 240

  tags = {
    Name        = local.parsing_dlq_name
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}

resource "aws_sqs_queue" "ehr_complete" {
  name                       = local.ehr_complete_queue_name
  message_retention_seconds  = local.max_retention_period
  kms_master_key_id          = aws_kms_key.ehr_complete.id
  receive_wait_time_seconds  = 20
  visibility_timeout_seconds = 240

  tags = {
    Name        = local.ehr_complete_queue_name
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}

resource "aws_sqs_queue" "ehr_complete_observability" {
  name                       = local.ehr_complete_observability_queue_name
  message_retention_seconds  = local.thirty_minute_retention_period
  kms_master_key_id          = aws_kms_key.ehr_complete.id
  receive_wait_time_seconds  = 20
  visibility_timeout_seconds = 240

  tags = {
    Name        = local.ehr_complete_observability_queue_name
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}

resource "aws_sqs_queue" "ehr_in_unhandled_observability" {
  name                       = local.ehr_in_unhandled_observability_queue_name
  message_retention_seconds  = local.thirty_minute_retention_period
  kms_master_key_id          = aws_kms_key.ehr_in_unhandled.id
  receive_wait_time_seconds  = 20
  visibility_timeout_seconds = 240

  tags = {
    Name        = local.ehr_in_unhandled_observability_queue_name
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}

resource "aws_sqs_queue" "ehr_transfer_service_audit_uploader" {
  name                       = local.ehr_transfer_service_audit_queue_name
  message_retention_seconds  = 1209600
  kms_master_key_id = aws_kms_key.ehr_transfer_audit_kms_key.id

  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.re_registration_audit_uploader_dlq.arn
    maxReceiveCount     = 4
  })
  tags = {
    Name = local.ehr_transfer_service_audit_queue_name
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}

resource "aws_sqs_queue" "re_registration_audit_uploader_dlq" {
  name                       = local.ehr_transfer_service_audit_dlq
  message_retention_seconds  = 1209600
  kms_master_key_id = aws_kms_key.ehr_transfer_audit_kms_key.id


  tags = {
    Name = local.ehr_transfer_service_audit_dlq
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}