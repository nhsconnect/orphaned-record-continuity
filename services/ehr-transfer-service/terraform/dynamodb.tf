resource "aws_dynamodb_table" "transfer_tracker" {
  name                        = "${var.environment}-${var.component_name}-transfer-tracker"
  billing_mode                = "PAY_PER_REQUEST"
  hash_key                    = "conversation_id"
#  deletion_protection_enabled = true

  server_side_encryption {
    enabled     = true
    kms_key_arn = aws_kms_key.transfer_tracker_dynamodb_kms_key.arn
  }

  point_in_time_recovery {
    enabled = true
  }

  attribute {
    name = "conversation_id"
    type = "S"
  }

  attribute {
    name = "nhs_number"
    type = "S"
  }

  attribute {
    name = "is_active"
    type = "S"
  }

  global_secondary_index {
    name            = "NhsNumberSecondaryIndex"
    hash_key        = "nhs_number"
    projection_type = "ALL"
  }

  global_secondary_index {
    name            = "IsActiveSecondaryIndex"
    hash_key        = "is_active"
    projection_type = "ALL"
  }
}
