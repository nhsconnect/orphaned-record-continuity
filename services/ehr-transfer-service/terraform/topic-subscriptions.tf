resource "aws_sns_topic_subscription" "repo_incoming_topic" {
  protocol             = "sqs"
  raw_message_delivery = true
  topic_arn            = data.aws_ssm_parameter.repo_incoming_topic_arn.value
  endpoint             = aws_sqs_queue.repo_incoming.arn
}

resource "aws_sns_topic_subscription" "negative_acks_topic" {
  protocol             = "sqs"
  raw_message_delivery = true
  topic_arn            = aws_sns_topic.negative_acks.arn
  endpoint             = aws_sqs_queue.negative_acks.arn
}

resource "aws_sns_topic_subscription" "negative_acks_observability_topic" {
  protocol             = "sqs"
  raw_message_delivery = true
  topic_arn            = aws_sns_topic.negative_acks.arn
  endpoint             = aws_sqs_queue.negative_acks_observability.arn
}

resource "aws_sns_topic_subscription" "small_ehr_topic" {
  protocol             = "sqs"
  raw_message_delivery = true
  topic_arn            = aws_sns_topic.small_ehr.arn
  endpoint             = aws_sqs_queue.small_ehr.arn
}

resource "aws_sns_topic_subscription" "small_ehr_observability_topic" {
  protocol             = "sqs"
  raw_message_delivery = true
  topic_arn            = aws_sns_topic.small_ehr.arn
  endpoint             = aws_sqs_queue.small_ehr_observability.arn
}

resource "aws_sns_topic_subscription" "large_ehr_topic" {
  protocol             = "sqs"
  raw_message_delivery = true
  topic_arn            = aws_sns_topic.large_ehr.arn
  endpoint             = aws_sqs_queue.large_ehr.arn
}

resource "aws_sns_topic_subscription" "large_ehr_observability_topic" {
  protocol             = "sqs"
  raw_message_delivery = true
  topic_arn            = aws_sns_topic.large_ehr.arn
  endpoint             = aws_sqs_queue.large_ehr_observability.arn
}

resource "aws_sns_topic_subscription" "large_message_fragments_topic" {
  protocol             = "sqs"
  raw_message_delivery = true
  topic_arn            = aws_sns_topic.large_message_fragments.arn
  endpoint             = aws_sqs_queue.large_message_fragments.arn
}

resource "aws_sns_topic_subscription" "large_message_fragments_observability_topic" {
  protocol             = "sqs"
  raw_message_delivery = true
  topic_arn            = aws_sns_topic.large_message_fragments.arn
  endpoint             = aws_sqs_queue.large_message_fragments_observability.arn
}

resource "aws_sns_topic_subscription" "positive_acks_observability_topic" {
  protocol             = "sqs"
  raw_message_delivery = true
  topic_arn            = aws_sns_topic.positive_acks.arn
  endpoint             = aws_sqs_queue.positive_acks_observability.arn
}

resource "aws_sns_topic_subscription" "parsing_dlq_topic" {
  protocol             = "sqs"
  raw_message_delivery = true
  topic_arn            = aws_sns_topic.parsing_dlq.arn
  endpoint             = aws_sqs_queue.parsing_dlq.arn
}

resource "aws_sns_topic_subscription" "ehr_complete_topic" {
  protocol             = "sqs"
  raw_message_delivery = true
  topic_arn            = aws_sns_topic.ehr_complete.arn
  endpoint             = aws_sqs_queue.ehr_complete.arn
}

resource "aws_sns_topic_subscription" "ehr_complete_observability_topic" {
  protocol             = "sqs"
  raw_message_delivery = true
  topic_arn            = aws_sns_topic.ehr_complete.arn
  endpoint             = aws_sqs_queue.ehr_complete_observability.arn
}


resource "aws_sns_topic_subscription" "ehr_transfer_audit" {
  protocol             = "sqs"
  raw_message_delivery = true
  topic_arn            = aws_sns_topic.splunk_uploader.arn
  endpoint             = aws_sqs_queue.ehr_transfer_service_audit_uploader.arn
}

resource "aws_sns_topic_subscription" "ehr_in_unhandled_topic" {
  protocol             = "sqs"
  raw_message_delivery = true
  topic_arn            = aws_sns_topic.ehr_in_unhandled.arn
  endpoint             = aws_sqs_queue.ehr_in_unhandled_observability.arn
}