environment = "dev"

task_cpu    = 512
task_memory = 1024

service_desired_count = "1"

enable_scale_action = false
scale_up_expression = "((MINUTE(m1)>=0)),10,0"

timeout_in_seconds= "300"
timeout_scheduler_fixed_rate_in_seconds="300"