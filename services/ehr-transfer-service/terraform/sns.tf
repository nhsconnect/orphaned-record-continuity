locals {
  sns_topic_arns = [
    aws_sns_topic.parsing_dlq.arn,
    aws_sns_topic.positive_acks.arn,
    aws_sns_topic.large_message_fragments.arn,
    aws_sns_topic.large_ehr.arn,
    aws_sns_topic.small_ehr.arn,
    aws_sns_topic.negative_acks.arn,
    aws_sns_topic.ehr_complete.arn,
    aws_sns_topic.transfer_complete.arn,
    aws_sns_topic.splunk_uploader.arn,
    aws_sns_topic.ehr_in_unhandled.arn
  ]
}

resource "aws_sns_topic" "negative_acks" {
  name                          = "${var.environment}-${var.component_name}-negative-acks-sns-topic"
  kms_master_key_id             = aws_kms_key.negative_acks.id
  sqs_failure_feedback_role_arn = aws_iam_role.sns_failure_feedback_role.arn

  tags = {
    Name        = "${var.environment}-${var.component_name}-negative-acks-sns-topic"
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}

resource "aws_sns_topic" "small_ehr" {
  name                          = "${var.environment}-${var.component_name}-small-ehr-sns-topic"
  kms_master_key_id             = aws_kms_key.small_ehr.id
  sqs_failure_feedback_role_arn = aws_iam_role.sns_failure_feedback_role.arn

  tags = {
    Name        = "${var.environment}-${var.component_name}-small-ehr-sns-topic"
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}

resource "aws_sns_topic" "large_ehr" {
  name                          = "${var.environment}-${var.component_name}-large-ehr-sns-topic"
  kms_master_key_id             = aws_kms_key.large_ehr.id
  sqs_failure_feedback_role_arn = aws_iam_role.sns_failure_feedback_role.arn

  tags = {
    Name        = "${var.environment}-${var.component_name}-large-ehr-sns-topic"
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}

resource "aws_sns_topic" "large_message_fragments" {
  name                          = "${var.environment}-${var.component_name}-large-message-fragments-sns-topic"
  kms_master_key_id             = aws_kms_key.large_message_fragments.id
  sqs_failure_feedback_role_arn = aws_iam_role.sns_failure_feedback_role.arn

  tags = {
    Name        = "${var.environment}-${var.component_name}-large-message-fragments-sns-topic"
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}

resource "aws_sns_topic" "positive_acks" {
  name                          = "${var.environment}-${var.component_name}-positive-acks-sns-topic"
  kms_master_key_id             = aws_kms_key.positive_acks.id
  sqs_failure_feedback_role_arn = aws_iam_role.sns_failure_feedback_role.arn

  tags = {
    Name        = "${var.environment}-${var.component_name}-positive-acks-sns-topic"
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}

resource "aws_sns_topic" "parsing_dlq" {
  name                          = "${var.environment}-${var.component_name}-parsing-dlq-sns-topic"
  kms_master_key_id             = aws_kms_key.parsing_dlq.id
  sqs_failure_feedback_role_arn = aws_iam_role.sns_failure_feedback_role.arn

  tags = {
    Name        = "${var.environment}-${var.component_name}-parsing-dlq-sns-topic"
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}

resource "aws_sns_topic" "ehr_complete" {
  name                          = "${var.environment}-${var.component_name}-ehr-complete-sns-topic"
  kms_master_key_id             = aws_kms_key.ehr_complete.id
  sqs_failure_feedback_role_arn = aws_iam_role.sns_failure_feedback_role.arn

  tags = {
    Name        = "${var.environment}-${var.component_name}-ehr-complete-sns-topic"
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}

resource "aws_sns_topic" "transfer_complete" {
  name                          = "${var.environment}-${var.component_name}-transfer-complete-sns-topic"
  kms_master_key_id             = aws_kms_key.transfer_complete.id
  sqs_failure_feedback_role_arn = aws_iam_role.sns_failure_feedback_role.arn

  tags = {
    Name        = "${var.environment}-${var.component_name}-transfer-complete-sns-topic"
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}

resource "aws_sns_topic" "ehr_in_unhandled" {
  name                          = "${var.environment}-${var.component_name}-unhandled-sns-topic"
  kms_master_key_id             = aws_kms_key.ehr_in_unhandled.id
  sqs_failure_feedback_role_arn = aws_iam_role.sns_failure_feedback_role.arn

  tags = {
    Name        = "${var.environment}-${var.component_name}-unhandled-sns-topic"
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}

resource "aws_sns_topic" "splunk_uploader" {
  name                          = "${var.environment}-${var.component_name}-splunk-uploader-sns-topic"
  kms_master_key_id             = aws_kms_key.ehr_transfer_audit_kms_key.id
  sqs_failure_feedback_role_arn = aws_iam_role.sns_failure_feedback_role.arn

  tags = {
    Name        = "${var.environment}-${var.component_name}-splunk-uploader-sns-topic"
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}

resource "aws_ssm_parameter" "ehr_in_unhandled_sns_topic" {
  name  = "/repo/${var.environment}/output/${var.component_name}/ehr-in-unhandled-sns-topic-arn"
  type  = "String"
  value = aws_sns_topic.ehr_in_unhandled.arn
}

resource "aws_sns_topic_policy" "deny_http" {
  for_each = toset(local.sns_topic_arns)

  arn = each.value

  policy = <<EOF
{
  "Version": "2008-10-17",
  "Id": "__default_policy_ID",
  "Statement": [
    {
      "Sid": "__default_statement_ID",
      "Effect": "Allow",
      "Principal": {
        "AWS": "*"
      },
      "Action": [
        "SNS:GetTopicAttributes",
        "SNS:SetTopicAttributes",
        "SNS:AddPermission",
        "SNS:RemovePermission",
        "SNS:DeleteTopic",
        "SNS:Subscribe",
        "SNS:ListSubscriptionsByTopic",
        "SNS:Publish",
        "SNS:Receive"
      ],
      "Resource": "${each.value}",
      "Condition": {
        "StringEquals": {
          "AWS:SourceOwner": "${data.aws_caller_identity.current.account_id}"
        }
      }
    },
    {
      "Sid": "DenyHTTPSubscription",
      "Effect": "Deny",
      "Principal": "*",
      "Action": "sns:Subscribe",
      "Resource": "${each.value}",
      "Condition": {
        "StringEquals": {
          "sns:Protocol": "http"
        }
      }
    },
    {
      "Sid": "DenyHTTPPublish",
      "Effect": "Deny",
      "Principal": "*",
      "Action": "SNS:Publish",
      "Resource": "${each.value}",
      "Condition": {
        "Bool": {
          "aws:SecureTransport": "false"
        }
      }
    }
  ]
}
EOF
}