resource "aws_kms_key" "sns_sqs_encryption" {
  description         = "Custom KMS Key to enable server side encryption for SNS and SQS"
  policy              = data.aws_iam_policy_document.sns_sqs_kms_key_policy_doc.json
  enable_key_rotation = true

  tags = {
    Name        = "${var.environment}-sns-sqs-encryption-kms-key"
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}

resource "aws_kms_alias" "sns_sqs_encryption" {
  name          = "alias/sns-sqs-encryption-kms-key"
  target_key_id = aws_kms_key.sns_sqs_encryption.id
}

data "aws_iam_policy_document" "sns_sqs_kms_key_policy_doc" {
  statement {
    effect = "Allow"

    principals {
      identifiers = ["arn:aws:iam::${data.aws_caller_identity.current.account_id}:root"]
      type        = "AWS"
    }
    actions   = ["kms:*"]
    resources = ["*"]
  }

  statement {
    effect = "Allow"

    principals {
      identifiers = ["sns.amazonaws.com"]
      type        = "Service"
    }

    actions = [
      "kms:Decrypt",
      "kms:GenerateDataKey*"
    ]

    resources = ["*"]
  }

  statement {
    effect = "Allow"

    principals {
      identifiers = ["cloudwatch.amazonaws.com"]
      type        = "Service"
    }

    actions = [
      "kms:Decrypt",
      "kms:GenerateDataKey*"
    ]

    resources = ["*"]
  }
}

resource "aws_sns_topic" "nems_events" {
  name                          = "${var.environment}-${var.component_name}-nems-events-sns-topic"
  kms_master_key_id             = aws_kms_key.sns_sqs_encryption.id
  sqs_failure_feedback_role_arn = aws_iam_role.sns_failure_feedback_role.arn

  tags = {
    Name        = "${var.environment}-${var.component_name}-nems-events-sns-topic"
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
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

resource "aws_sqs_queue" "observability" {
  name                      = "${var.environment}-${var.component_name}-nems-events-observability"
  message_retention_seconds = 1800
  kms_master_key_id         = aws_kms_key.sns_sqs_encryption.id

  tags = {
    Name        = "${var.environment}-${var.component_name}-nems-events-observability"
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}

resource "aws_ssm_parameter" "nems_events_observability" {
  name  = "/repo/${var.environment}/output/${var.component_name}/nems-events-observability"
  type  = "String"
  value = aws_sqs_queue.observability.name
  tags = {
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}

resource "aws_sns_topic_subscription" "nems_events_to_observability" {
  protocol             = "sqs"
  raw_message_delivery = true
  topic_arn            = aws_sns_topic.nems_events.arn
  endpoint             = aws_sqs_queue.observability.arn
}

resource "aws_sqs_queue_policy" "nems_events_subscription" {
  queue_url = aws_sqs_queue.observability.id
  policy    = data.aws_iam_policy_document.sqs_policy_doc.json
}

data "aws_iam_policy_document" "sqs_policy_doc" {
  statement {

    effect = "Allow"

    actions = [
      "sqs:SendMessage"
    ]

    principals {
      identifiers = ["sns.amazonaws.com"]
      type        = "Service"
    }

    resources = [
      aws_sqs_queue.observability.arn
    ]

    condition {
      test     = "ArnEquals"
      values   = [aws_sns_topic.nems_events.arn]
      variable = "aws:SourceArn"
    }
  }
}


resource "aws_ssm_parameter" "sns_sqs_kms_key_id" {
  name  = "/repo/${var.environment}/output/${var.repo_name}/sns-sqs-kms-key-id"
  type  = "String"
  value = aws_kms_key.sns_sqs_encryption.id

  tags = {
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}

resource "aws_ssm_parameter" "nems_events_topic_arn" {
  name  = "/repo/${var.environment}/output/${var.repo_name}/nems-events-topic-arn"
  type  = "String"
  value = aws_sns_topic.nems_events.arn

  tags = {
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}
