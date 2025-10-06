environment = "perf"

task_cpu    = 512
task_memory = 1024

service_desired_count = "3"

enable_scale_action = false
scale_up_expression = "((MINUTE(m1)>=0)),10,0"

log_level = "info"