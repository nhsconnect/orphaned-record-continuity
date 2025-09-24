locals {
  task_role_arn       = aws_iam_role.ehr-repo.arn
  task_execution_role = "arn:aws:iam::${data.aws_caller_identity.current.account_id}:role/${var.environment}-${var.component_name}-EcsTaskRole"
  task_ecr_url        = "${data.aws_caller_identity.current.account_id}.dkr.ecr.${var.region}.amazonaws.com"
  task_log_group      = "/nhs/deductions/${var.environment}-${data.aws_caller_identity.current.account_id}/${var.component_name}"
  environment_variables = [
    { name = "NODE_ENV", value = var.node_env },
    { name = "NHS_ENVIRONMENT", value = var.environment },
    { name = "S3_BUCKET_NAME", value = var.s3_bucket_name },
    { name = "AWS_REGION", value = var.region },
    { name = "LOG_LEVEL", value = var.log_level },
    { name = "DYNAMODB_NAME", value = data.aws_ssm_parameter.dynamodb_name.value },
  ]
}

resource "aws_ecs_task_definition" "task" {
  family                   = var.component_name
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = var.task_cpu
  memory                   = var.task_memory
  execution_role_arn       = local.task_execution_role
  task_role_arn            = local.task_role_arn


  container_definitions = templatefile("${path.module}/templates/ecs-task-def.tmpl", {
    container_name        = "${var.component_name}-container",
    ecr_url               = local.task_ecr_url,
    image_name            = "deductions/${var.component_name}",
    image_tag             = var.task_image_tag,
    cpu                   = var.task_cpu,
    memory                = var.task_memory,
    container_port        = var.port,
    host_port             = var.port,
    log_region            = var.region,
    log_group             = local.task_log_group,
    environment_variables = jsonencode(local.environment_variables)
  })

  tags = {
    Environment = var.environment
    CreatedBy   = var.repo_name
  }
}

resource "aws_security_group" "ecs-tasks-sg" {
  name   = "${var.environment}-${var.component_name}-ecs-tasks-sg"
  vpc_id = data.aws_ssm_parameter.deductions_core_vpc_id.value

  ingress {
    description = "Allow traffic from internal ALB of EHR Repo"
    protocol    = "tcp"
    from_port   = "3000"
    to_port     = "3000"
    security_groups = [
      aws_security_group.ehr_repo_alb.id
    ]
  }

  egress {
    description = "Allow All Outbound in deductions-private vpc"
    protocol    = "-1"
    from_port   = 0
    to_port     = 0
    cidr_blocks = [data.aws_vpc.private_vpc.cidr_block]
  }

  egress {
    description = "Allow All Outbound in deductions-core vpc"
    protocol    = "-1"
    from_port   = 0
    to_port     = 0
    cidr_blocks = [data.aws_vpc.deductions_core_vpc.cidr_block]
  }

  egress {
    description = "Allow All Outbound in mhs vpc"
    protocol    = "-1"
    from_port   = 0
    to_port     = 0
    cidr_blocks = [data.aws_vpc.mhs.cidr_block]
  }

  egress {
    description     = "Allow outbound HTTPS traffic to dynamodb"
    protocol        = "tcp"
    from_port       = 443
    to_port         = 443
    prefix_list_ids = [data.aws_ssm_parameter.dynamodb_prefix_list_id.value]
  }

  egress {
    description     = "Allow outbound HTTPS traffic to s3"
    protocol        = "tcp"
    from_port       = 443
    to_port         = 443
    prefix_list_ids = [data.aws_ssm_parameter.s3_prefix_list_id.value]
  }

  tags = {
    Name        = "${var.environment}-${var.component_name}-ecs-tasks-sg"
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}

data "aws_vpc" "deductions_core_vpc" {
  id = data.aws_ssm_parameter.deductions_core_vpc_id.value
}

data "aws_vpc" "private_vpc" {
  id = data.aws_ssm_parameter.deductions_private_vpc_id.value
}

data "aws_vpc" "mhs" {
  filter {
    name   = "tag:Name"
    values = ["${var.environment}-repo-mhs-vpc"]
  }
}

data "aws_ssm_parameter" "dynamodb_prefix_list_id" {
  name = "/repo/${var.environment}/output/prm-deductions-infra/deductions-core/dynamodb_prefix_list_id"
}

data "aws_ssm_parameter" "s3_prefix_list_id" {
  name = "/repo/${var.environment}/output/prm-deductions-infra/deductions-core/s3-prefix-list-id"
}

resource "aws_security_group" "vpn_to_ehr_repo_ecs" {
  count       = var.allow_vpn_to_ecs_tasks ? 1 : 0
  name        = "${var.environment}-vpn-to-${var.component_name}-ecs"
  description = "controls access from vpn to ehr-repo ecs task"
  vpc_id      = data.aws_ssm_parameter.deductions_core_vpc_id.value

  ingress {
    description     = "Allow vpn to access EHR Repo ECS"
    protocol        = "tcp"
    from_port       = "3000"
    to_port         = "3000"
    security_groups = [data.aws_ssm_parameter.vpn_sg_id.value]
  }

  tags = {
    Name        = "${var.environment}-vpn-to-${var.component_name}-sg"
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}

resource "aws_ssm_parameter" "deductions_core_ecs_tasks_sg_id" {
  name  = "/repo/${var.environment}/output/${var.repo_name}/deductions-core-ecs-tasks-sg-id"
  type  = "String"
  value = aws_security_group.ecs-tasks-sg.id
  tags = {
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}
