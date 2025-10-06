locals {
  sqs_large_messages_bucket_name       = "${var.environment}-${var.component_name}-sqs-large-messages"
  sqs_large_messages_access_log_prefix = "s3-access-logs/"
}
resource "aws_s3_bucket" "sqs_large_message_bucket" {
  bucket        = local.sqs_large_messages_bucket_name
  acl           = "private"
  force_destroy = true

  versioning {
    enabled = false
  }

  logging {
    target_bucket = aws_s3_bucket.sqs_large_messages_s3_access_logs.id
    target_prefix = local.sqs_large_messages_access_log_prefix
  }

  server_side_encryption_configuration {
    rule {
      apply_server_side_encryption_by_default {
        sse_algorithm = "AES256"
      }
    }
  }

  tags = {
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}

resource "aws_s3_bucket_policy" "ehr-repo-sqs_large_message_bucket_policy" {
  bucket = aws_s3_bucket.sqs_large_message_bucket.id
  policy = jsonencode({
    "Version" : "2008-10-17"
    "Statement" : [
      {
        Effect : "Deny",
        Principal : "*",
        Action : "s3:*",
        Resource : "${aws_s3_bucket.sqs_large_message_bucket.arn}/*",
        Condition : {
          Bool : {
            "aws:SecureTransport" : "false"
          }
        }
      },
      {
        Effect : "Deny",
        Principal : {
          "AWS" : "arn:aws:iam::${data.aws_caller_identity.current.account_id}:role/NHSDAdminRole"
        },
        Action : "s3:*",
        Resource : [
          "${aws_s3_bucket.sqs_large_message_bucket.arn}",
          "${aws_s3_bucket.sqs_large_message_bucket.arn}/*"
        ]
      },
    ]
  })
}

resource "aws_s3_bucket_public_access_block" "sqs_large_message_bucket" {
  bucket = aws_s3_bucket.sqs_large_message_bucket.bucket

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

resource "aws_s3_bucket" "sqs_large_messages_s3_access_logs" {
  bucket        = "${local.sqs_large_messages_bucket_name}-access-logs"
  acl           = "private"
  force_destroy = true
  versioning {
    enabled = true
  }

  server_side_encryption_configuration {
    rule {
      apply_server_side_encryption_by_default {
        sse_algorithm = "AES256"
      }
    }
  }
  tags = {
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}

resource "aws_s3_bucket_policy" "ehr-repo-sqs_large_message_bucket_access_logs_policy" {
  bucket = aws_s3_bucket.sqs_large_messages_s3_access_logs.id
  policy = jsonencode({
    "Version" : "2012-10-17",
    "Statement" : [
      {
        "Sid" : "S3ServerAccessLogsPolicy",
        "Effect" : "Allow",
        "Principal" : {
          "Service" : "logging.s3.amazonaws.com"
        },
        "Action" : "s3:PutObject",
        "Resource" : "${aws_s3_bucket.sqs_large_messages_s3_access_logs.arn}/${local.sqs_large_messages_access_log_prefix}*"
        "Condition" : {
          "ArnLike" : {
            "aws:SourceArn" : aws_s3_bucket.sqs_large_message_bucket.arn
          },
          "StringEquals" : {
            "aws:SourceAccount" : local.account_id
          }
        }
      },
      {
        "Sid" : "S3EnforceHTTPSPolicy",
        "Effect" : "Deny",
        "Principal" : "*",
        "Action" : "s3:*",
        "Resource" : [
          aws_s3_bucket.sqs_large_messages_s3_access_logs.arn,
          "${aws_s3_bucket.sqs_large_messages_s3_access_logs.arn}/*"
        ],
        "Condition" : {
          "Bool" : {
            "aws:SecureTransport" : "false"
          }
        }
      }
    ]
  })
}

resource "aws_s3_bucket_public_access_block" "sqs_large_messages_access_logs" {
  bucket = aws_s3_bucket.sqs_large_messages_s3_access_logs.bucket

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}