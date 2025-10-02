provider "aws" {
  profile   = "default"
  region    = var.region
}

terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "3.44.0"
    }
  }
}
