locals {
  ecs_cluster_id  = aws_ecs_cluster.mesh-forwarder-ecs-cluster.id
  ecs_task_sg_id = aws_security_group.mesh-forwarder-ecs-tasks-sg.id
  private_subnets = split(",", data.aws_ssm_parameter.deductions_private_private_subnets.value)
}

resource "aws_ecs_service" "mesh_forwarder" {
  name            = "${var.environment}-${var.component_name}-service"
  cluster         = local.ecs_cluster_id
  task_definition = aws_ecs_task_definition.forwarder.arn
  desired_count   = var.service_desired_count
  launch_type     = "FARGATE"

  network_configuration {
    security_groups = [local.ecs_task_sg_id]
    subnets         = local.private_subnets
  }
}

resource "aws_ecs_cluster" "mesh-forwarder-ecs-cluster" {
  name = "${var.environment}-${var.component_name}-ecs-cluster"

  setting {
    name  = "containerInsights"
    value = "enabled"
  }

  tags = {
    Name = "${var.environment}-${var.component_name}"
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}

resource "aws_ssm_parameter" "cluster_name" {
  name  = "/repo/${var.environment}/output/${var.component_name}/mesh-forwarder-ecs-cluster-name"
  type  = "String"
  value = aws_ecs_cluster.mesh-forwarder-ecs-cluster.name
}
