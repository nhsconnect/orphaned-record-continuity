locals {
  task_role_arn       = aws_iam_role.mesh_forwarder.arn
  task_execution_role = "arn:aws:iam::${data.aws_caller_identity.current.account_id}:role/${var.environment}-${var.component_name}-EcsTaskRole"
  task_ecr_url        = "${data.aws_caller_identity.current.account_id}.dkr.ecr.${var.region}.amazonaws.com"
  task_log_group      = "/nhs/deductions/${var.environment}-${data.aws_caller_identity.current.account_id}/${var.component_name}"
  environment_variables = [
    { name : "MESH_URL", value : var.mesh_url },
    { name : "MESH_MAILBOX_SSM_PARAM_NAME", value : var.mesh_mailbox_ssm_param_name },
    { name : "MESH_PASSWORD_SSM_PARAM_NAME", value : var.mesh_password_ssm_param_name },
    { name : "MESH_SHARED_KEY_SSM_PARAM_NAME", value : var.mesh_shared_key_ssm_param_name },
    { name : "MESH_CLIENT_CERT_SSM_PARAM_NAME", value : var.mesh_client_cert_ssm_param_name },
    { name : "MESH_CLIENT_KEY_SSM_PARAM_NAME", value : var.mesh_client_key_ssm_param_name },
    { name : "MESH_CA_CERT_SSM_PARAM_NAME", value : var.mesh_ca_cert_ssm_param_name },
    { name : "SNS_TOPIC_ARN", value : aws_sns_topic.nems_events.arn },
    { name : "MESSAGE_DESTINATION", value : var.message_destination },
    { name : "DISABLE_MESSAGE_HEADER_VALIDATION", value : var.disable_message_header_validation },
    { name : "POLL_FREQUENCY", value: var.poll_frequency }
  ]
}

resource "aws_ecs_task_definition" "forwarder" {
  family = var.component_name
  container_definitions = jsonencode([
    {
      name  = "mesh-forwarder"
      image = "${data.aws_ecr_repository.mesh_s3_forwarder.repository_url}:${var.task_image_tag}"
      environment = local.environment_variables
      essential = true
      logConfiguration = {
        logDriver = "awslogs"
        options = {
          awslogs-group         = aws_cloudwatch_log_group.log_group.name
          awslogs-region        = var.region
          awslogs-stream-prefix = var.task_image_tag
        }
      }
    }
  ])
  cpu                      = var.task_cpu
  memory                   = var.task_memory
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  tags = merge(

  {
    Name = "${var.environment}-mesh-forwarder"
  }
  )
  execution_role_arn = aws_iam_role.ecs_execution.arn
  task_role_arn      = aws_iam_role.mesh_forwarder.arn
}

resource "aws_security_group" "mesh-forwarder-ecs-tasks-sg" {
  name        = "${var.environment}-${var.component_name}-ecs-tasks-sg"
  vpc_id      = data.aws_ssm_parameter.deductions_private_vpc_id.value

  egress {
    description = "Allow all outbound HTTPS traffic"
    protocol         = "tcp"
    from_port        = 443
    to_port          = 443
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${var.environment}-${var.component_name}-ecs-tasks-sg"
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}


data "aws_ecr_repository" "mesh_s3_forwarder" {
  name = "deductions/${var.component_name}"
}
