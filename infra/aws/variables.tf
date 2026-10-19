variable "region" {
  type    = string
  default = "ap-northeast-2"
}

variable "budget_email" {
  type        = string
  description = "Email address for AWS budget alerts."
}

variable "monthly_budget_usd" {
  type        = number
  description = "USD alert threshold. Recheck the KRW exchange rate before applying."
  default     = 65
}

variable "github_repository" {
  type    = string
  default = "strangehoon/cinema"
}
