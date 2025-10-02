locals {
  inbox_message_count_metric_name = "MeshInboxMessageCount"
  error_logs_metric_name          = "ErrorCountInLogs"
  sns_topic_error_logs_metric_name = "NumberOfNotificationsFailed"
  mesh_forwarder_metric_namespace = "MeshForwarder"
  sns_topic_namespace = "AWS/SNS"
  mesh_forwarder_sns_topic_name = "${var.environment}-mesh-forwarder-nems-events-sns-topic"
  alarm_actions = [data.aws_sns_topic.alarm_notifications.arn]
}

resource "aws_cloudwatch_log_group" "log_group" {
  name = "/nhs/deductions/${var.environment}-${data.aws_caller_identity.current.account_id}/${var.component_name}"

  tags = {
    Environment = var.environment
    CreatedBy   = var.repo_name
  }
}

resource "aws_cloudwatch_log_metric_filter" "inbox_message_count" {
  name           = "${var.environment}-mesh-inbox-message-count"
  pattern        = "{ $.event = \"COUNT_MESSAGES\" }"
  log_group_name = aws_cloudwatch_log_group.log_group.name

  metric_transformation {
    name      = local.inbox_message_count_metric_name
    namespace = local.mesh_forwarder_metric_namespace
    value     = "$.inboxMessageCount"
  }
}

resource "aws_cloudwatch_metric_alarm" "inbox-messages-not-consumed" {
  alarm_name          = "${var.environment}-mesh-inbox-messages-not-consumed"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = var.cloudwatch_alarm_evaluation_periods
  metric_name         = local.inbox_message_count_metric_name
  namespace           = local.mesh_forwarder_metric_namespace
  period              = "60"
  statistic           = "Minimum"
  threshold           = "0"
  alarm_description   = "This alarm is triggered if the mailbox doesn't get empty in a given evaluation time period"
  treat_missing_data  = "breaching"
  actions_enabled     = "true"
  alarm_actions       = local.alarm_actions
  ok_actions          = local.alarm_actions
}

resource "aws_cloudwatch_log_metric_filter" "error_log_metric_filter" {
  name           = "${var.environment}-${var.component_name}-error-logs"
  pattern        = "{ $.error = * }"
  log_group_name = aws_cloudwatch_log_group.log_group.name

  metric_transformation {
    name          = local.error_logs_metric_name
    namespace     = local.mesh_forwarder_metric_namespace
    value         = 1
    default_value = 0
  }
}

resource "aws_cloudwatch_metric_alarm" "error_log_alarm" {
  alarm_name                = "${var.environment}-${var.component_name}-error-logs"
  comparison_operator       = "GreaterThanThreshold"
  threshold                 = "0"
  evaluation_periods        = "1"
  period                    = "60"
  metric_name               = local.error_logs_metric_name
  namespace                 = local.mesh_forwarder_metric_namespace
  statistic                 = "Sum"
  alarm_description         = "This alarm monitors errors logs in ${var.component_name}"
  treat_missing_data        = "notBreaching"
  actions_enabled           = "true"
  alarm_actions             = local.alarm_actions
  ok_actions                = local.alarm_actions
}

resource "aws_cloudwatch_metric_alarm" "sns_topic_error_log_alarm" {
  alarm_name                = "${local.mesh_forwarder_sns_topic_name}-error-logs"
  comparison_operator       = "GreaterThanThreshold"
  threshold                 = "0"
  evaluation_periods        = "1"
  period                    = "60"
  metric_name               = local.sns_topic_error_logs_metric_name
  namespace                 = local.sns_topic_namespace
  dimensions = {
    TopicName = local.mesh_forwarder_sns_topic_name
  }
  statistic                 = "Sum"
  alarm_description         = "This alarm monitors errors logs in ${local.mesh_forwarder_sns_topic_name}"
  treat_missing_data        = "notBreaching"
  actions_enabled           = "true"
  alarm_actions             = local.alarm_actions
  ok_actions                = local.alarm_actions
}