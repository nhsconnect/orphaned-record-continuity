resource "aws_ssm_parameter" "transfer_complete_kms_key" {
  name  = "/repo/${var.environment}/output/${var.component_name}/transfer-complete-encryption-kms-key"
  type  = "String"
  value = aws_kms_key.transfer_complete.id

  tags = {
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}

resource "aws_ssm_parameter" "transfer_complete_topic_arn" {
  name  = "/repo/${var.environment}/output/${var.component_name}/transfer-complete-sns-topic-arn"
  type  = "String"
  value = aws_sns_topic.transfer_complete.arn
}
