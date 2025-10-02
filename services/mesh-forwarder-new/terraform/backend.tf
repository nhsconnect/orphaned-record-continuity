terraform {
      backend "s3" {
        bucket = "prm-deductions-terraform-state"
        key    = "mesh-forwarder/terraform.tfstate"
        region = var.region
        encrypt = true
    }
}
