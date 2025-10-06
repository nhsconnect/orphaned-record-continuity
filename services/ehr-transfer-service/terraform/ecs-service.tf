locals {
  private_subnets = split(",", data.aws_ssm_parameter.deductions_private_private_subnets.value)
  ports_egress = [
    443,
    61617
  ]
}

resource "aws_ecs_service" "ecs-service" {
  name            = "${var.environment}-${var.component_name}"
  cluster         = aws_ecs_cluster.ehr_transfer_service_ecs_cluster.id
  task_definition = aws_ecs_task_definition.task.arn
  desired_count   = var.service_desired_count
  launch_type     = "FARGATE"

  network_configuration {
    security_groups = [aws_security_group.ehr-transfer-service-ecs-task-sg.id]
    subnets         = local.private_subnets
  }
}

data "aws_ssm_parameter" "service-to-ehr-repo-sg-id" {
  name = "/repo/${var.environment}/output/prm-deductions-ehr-repository/service-to-ehr-repo-sg-id"
}

resource "aws_security_group_rule" "ehr-transfer-service-to-ehr-repo" {
  type                     = "ingress"
  protocol                 = "TCP"
  from_port                = 443
  to_port                  = 443
  security_group_id        = data.aws_ssm_parameter.service-to-ehr-repo-sg-id.value
  source_security_group_id = aws_security_group.ehr-transfer-service-ecs-task-sg.id
}

data "aws_ssm_parameter" "service-to-mq-sg-id" {
  name = "/repo/${var.environment}/output/prm-deductions-infra/service-to-mq-sg-id"
}

resource "aws_security_group_rule" "ehr-transfer-service-to-mq" {
  type                     = "ingress"
  protocol                 = "tcp"
  from_port                = "61617"
  to_port                  = "61617"
  security_group_id        = data.aws_ssm_parameter.service-to-mq-sg-id.value
  source_security_group_id = aws_security_group.ehr-transfer-service-ecs-task-sg.id
}

resource "aws_ecs_cluster" "ehr_transfer_service_ecs_cluster" {
  name = "${var.environment}-${var.component_name}-ecs-cluster"

  setting {
    name  = "containerInsights"
    value = "enabled"
  }

  tags = {
    Name        = "${var.environment}-${var.component_name}"
    Environment = var.environment
    CreatedBy   = var.repo_name
  }
}

resource "aws_security_group_rule" "ehr-transfer-service-to-gp2gp-messenger" {
  type                     = "ingress"
  protocol                 = "TCP"
  from_port                = 443
  to_port                  = 443
  security_group_id        = data.aws_ssm_parameter.service-to-gp2gp-messenger-sg-id.value
  source_security_group_id = aws_security_group.ehr-transfer-service-ecs-task-sg.id
}

resource "aws_security_group" "ehr-transfer-service-ecs-task-sg" {
  name   = "${var.environment}-${var.component_name}-ecs-task-sg"
  vpc_id = data.aws_ssm_parameter.deductions_private_vpc_id.value


  dynamic "egress" {
    for_each = local.ports_egress
    content {
      description = "Allow HTTPS and MQ traffic outbound to deductions private and deductions core"
      protocol    = "TCP"
      from_port   = egress.value
      to_port     = egress.value
      cidr_blocks = [data.aws_vpc.deductions-private.cidr_block, data.aws_vpc.deductions-core.cidr_block]
    }
  }

  egress {
    description     = "Allow HTTPS traffic outbound to VPC Endpoints"
    protocol        = "TCP"
    from_port       = 443
    to_port         = 443
    security_groups = concat(tolist(data.aws_vpc_endpoint.ecr-dkr.security_group_ids), tolist(data.aws_vpc_endpoint.ecr-api.security_group_ids),
    tolist(data.aws_vpc_endpoint.logs.security_group_ids), tolist(data.aws_vpc_endpoint.ssm.security_group_ids))
  }

  egress {
    description = "Allow HTTPS traffic outbound to S3 VPC Endpoint"
    protocol    = "TCP"
    from_port   = 443
    to_port     = 443
    cidr_blocks = data.aws_vpc_endpoint.s3.cidr_blocks
  }

  egress {
    description     = "Allow outbound HTTPS traffic to dynamodb"
    protocol        = "TCP"
    from_port       = 443
    to_port         = 443
    prefix_list_ids = [data.aws_ssm_parameter.dynamodb_prefix_list_id.value]
  }

  tags = {
    Name        = "${var.environment}-ehr-transfer-service-ecs-task-sg"
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}


data "aws_vpc" "deductions-private" {
  id = data.aws_ssm_parameter.deductions_private_vpc_id.value
}

data "aws_vpc" "deductions-core" {
  id = data.aws_ssm_parameter.deductions_core_vpc_id.value
}

data "aws_vpc_endpoint" "ecr-dkr" {
  vpc_id       = data.aws_ssm_parameter.deductions_private_vpc_id.value
  service_name = "com.amazonaws.${var.region}.ecr.dkr"
}

data "aws_vpc_endpoint" "ecr-api" {
  vpc_id       = data.aws_ssm_parameter.deductions_private_vpc_id.value
  service_name = "com.amazonaws.${var.region}.ecr.api"
}

data "aws_vpc_endpoint" "logs" {
  vpc_id       = data.aws_ssm_parameter.deductions_private_vpc_id.value
  service_name = "com.amazonaws.${var.region}.logs"
}

data "aws_vpc_endpoint" "ssm" {
  vpc_id       = data.aws_ssm_parameter.deductions_private_vpc_id.value
  service_name = "com.amazonaws.${var.region}.ssm"
}

data "aws_vpc_endpoint" "s3" {
  vpc_id       = data.aws_ssm_parameter.deductions_private_vpc_id.value
  service_name = "com.amazonaws.${var.region}.s3"
}

data "aws_ssm_parameter" "dynamodb_prefix_list_id" {
  name = "/repo/${var.environment}/output/prm-deductions-infra/dynamodb_prefix_list_id"
}

data "aws_ssm_parameter" "service-to-gp2gp-messenger-sg-id" {
  name = "/repo/${var.environment}/output/prm-deductions-gp2gp-messenger/service-to-gp2gp-messenger-sg-id"
}