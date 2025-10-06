resource "aws_kms_key" "negative_acks" {
  description = "Custom KMS Key to enable server side encryption for negative acknowledgements SQS queue"
  policy      = data.aws_iam_policy_document.kms_key_policy_doc.json
  enable_key_rotation = true

  tags = {
    Name        = "${var.environment}-${var.component_name}-negative-acks-encryption-kms-key"
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}

resource "aws_kms_alias" "negative_acks_encryption" {
  name          = "alias/negative-acks-queue-encryption-kms-key"
  target_key_id = aws_kms_key.negative_acks.id
}

resource "aws_kms_key" "small_ehr" {
  description = "Custom KMS Key to enable server side encryption for small EHRs SQS queue"
  policy      = data.aws_iam_policy_document.kms_key_policy_doc.json
  enable_key_rotation = true

  tags = {
    Name        = "${var.environment}-${var.component_name}-small-ehr-encryption-kms-key"
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}

resource "aws_kms_alias" "small_ehr_encryption" {
  name          = "alias/small-ehr-queue-encryption-kms-key"
  target_key_id = aws_kms_key.small_ehr.id
}

resource "aws_kms_key" "large_ehr" {
  description = "Custom KMS Key to enable server side encryption for large EHRs SQS queue"
  policy      = data.aws_iam_policy_document.kms_key_policy_doc.json
  enable_key_rotation = true

  tags = {
    Name        = "${var.environment}-${var.component_name}-large-ehr-encryption-kms-key"
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}

resource "aws_kms_alias" "large_ehr_encryption" {
  name          = "alias/large-ehr-queue-encryption-kms-key"
  target_key_id = aws_kms_key.large_ehr.id
}

resource "aws_kms_key" "large_message_fragments" {
  description = "Custom KMS Key to enable server side encryption for large-message-fragments SQS queue"
  policy      = data.aws_iam_policy_document.kms_key_policy_doc.json
  enable_key_rotation = true

  tags = {
    Name        = "${var.environment}-${var.component_name}-large-message-fragments-encryption-kms-key"
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}

resource "aws_kms_alias" "large_message_fragments_encryption" {
  name          = "alias/large-message-fragments-queue-encryption-kms-key"
  target_key_id = aws_kms_key.large_message_fragments.id
}

resource "aws_kms_key" "positive_acks" {
  description = "Custom KMS Key to enable server side encryption for positive acknowledgements SQS queue"
  policy      = data.aws_iam_policy_document.kms_key_policy_doc.json
  enable_key_rotation = true

  tags = {
    Name        = "${var.environment}-${var.component_name}-positive-acks-encryption-kms-key"
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}

resource "aws_kms_alias" "positive_acks_encryption" {
  name          = "alias/positive-acks-queue-encryption-kms-key"
  target_key_id = aws_kms_key.positive_acks.id
}

resource "aws_kms_key" "parsing_dlq" {
  description = "Custom KMS Key to enable server side encryption for parsing DLQ"
  policy      = data.aws_iam_policy_document.kms_key_policy_doc.json
  enable_key_rotation = true

  tags = {
    Name        = "${var.environment}-${var.component_name}-parsing-dlq-encryption-kms-key"
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}

resource "aws_kms_alias" "parsing_dlq_encryption" {
  name          = "alias/parsing-dlq-encryption-kms-key"
  target_key_id = aws_kms_key.parsing_dlq.id
}

resource "aws_kms_key" "ehr_complete" {
  description = "Custom KMS Key to enable server side encryption for ehr-complete"
  policy      = data.aws_iam_policy_document.kms_key_policy_doc.json
  enable_key_rotation = true

  tags = {
    Name        = "${var.environment}-${var.component_name}-ehr-complete-encryption-kms-key"
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}

resource "aws_kms_alias" "ehr_complete_encryption" {
  name          = "alias/ehr-complete-encryption-kms-key"
  target_key_id = aws_kms_key.ehr_complete.id
}

resource "aws_kms_key" "transfer_complete" {
  description = "Custom KMS Key to enable server side encryption for transfer-complete"
  policy      = data.aws_iam_policy_document.kms_key_policy_doc.json
  enable_key_rotation = true

  tags = {
    Name        = "${var.environment}-${var.component_name}-transfer-complete-encryption-kms-key"
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}

resource "aws_kms_alias" "transfer_complete_encryption" {
  name          = "alias/transfer-complete-encryption-kms-key"
  target_key_id = aws_kms_key.transfer_complete.id
}

resource "aws_kms_key" "transfer_tracker_dynamodb_kms_key" {
  description = "Custom KMS Key to enable server side encryption for Transfer Tracker DB"
  policy      = data.aws_iam_policy_document.kms_key_policy_doc.json
  enable_key_rotation = true

  tags = {
    Name        = "${var.environment}-${var.component_name}-transfer-tracker-dynamodb-kms-key"
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}

resource "aws_kms_alias" "transfer_tracker_dynamodb_encryption" {
  name          = "alias/transfer-tracker-dynamodb-encryption-kms-key"
  target_key_id = aws_kms_key.transfer_tracker_dynamodb_kms_key.id
}

resource "aws_kms_key" "ehr_transfer_audit_kms_key" {
  description = "Custom KMS Key to enable server side encryption for Transfer Tracker DB"
  policy      = data.aws_iam_policy_document.kms_key_policy_doc.json
  enable_key_rotation = true

  tags = {
    Name        = "${var.environment}-${var.component_name}-ehr-transfer-audit-kms-key"
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}

resource "aws_kms_alias" "ehr_transfer_audit_encryption" {
  name          = "alias/ehr-transfer-audit-encryption-kms-key"
  target_key_id = aws_kms_key.ehr_transfer_audit_kms_key.id
}

resource "aws_kms_key" "ehr_in_unhandled" {
  description = "Custom KMS Key to enable server side encryption for ehr-in-unhandled"
  policy      = data.aws_iam_policy_document.kms_key_policy_doc.json
  enable_key_rotation = true

  tags = {
    Name        = "${var.environment}-${var.component_name}-unhandled-encryption-kms-key"
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}

resource "aws_kms_alias" "ehr_in_unhandled_encryption" {
  name          = "alias/ehr-in-unhandled-encryption-kms-key"
  target_key_id = aws_kms_key.ehr_in_unhandled.id
}


data "aws_iam_policy_document" "kms_key_policy_doc" {
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