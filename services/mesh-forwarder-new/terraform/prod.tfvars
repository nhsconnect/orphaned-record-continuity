environment    = "prod"

# must not be more than 1 causes issues reading from mailbox
service_desired_count = "1"

cloudwatch_alarm_evaluation_periods = 30
log_level = "info"

poll_frequency = "60"

mesh_url = "https://mesh-sync.spineservices.nhs.uk"
mesh_mailbox_ssm_param_name = "/repo/prod/user-input/external/mesh-mailbox-id"
mesh_password_ssm_param_name = "/repo/prod/user-input/external/mesh-mailbox-password"
mesh_shared_key_ssm_param_name = "/repo/prod/user-input/external/mesh-mailbox-shared-secret"
mesh_client_cert_ssm_param_name = "/repo/prod/user-input/external/mesh-mailbox-client-cert"
mesh_client_key_ssm_param_name = "/repo/prod/user-input/external/mesh-mailbox-client-key"
mesh_ca_cert_ssm_param_name = "/repo/prod/user-input/external/mesh-mailbox-ca-cert"
