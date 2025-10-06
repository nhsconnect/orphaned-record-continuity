locals {
  error_logs_metric_name              = "ErrorCountInLogs"
  ehr_transfer_service_metric_namespace = "EhrTransferService"
  sns_topic_namespace = "AWS/SNS"
  sqs_namespace = "AWS/SQS"
  sns_topic_error_logs_metric_name = "NumberOfNotificationsFailed"
  ehr_complete_sns_topic_name        = aws_sns_topic.ehr_complete.name
}

data "aws_sns_topic" "alarm_notifications" {
  name = "${var.environment}-alarm-notifications-sns-topic"
}

resource "aws_cloudwatch_log_group" "log_group" {
  name = "/nhs/deductions/${var.environment}-${data.aws_caller_identity.current.account_id}/${var.component_name}"

  tags = {
    Environment = var.environment
    CreatedBy   = var.repo_name
  }
}

resource "aws_cloudwatch_log_metric_filter" "log_metric_filter" {
  name           = "${var.environment}-${var.component_name}-error-logs"
  pattern        = "{ $.level = \"ERROR\" }"
  log_group_name = aws_cloudwatch_log_group.log_group.name

  metric_transformation {
    name          = local.error_logs_metric_name
    namespace     = local.ehr_transfer_service_metric_namespace
    value         = 1
    default_value = 0
  }
}