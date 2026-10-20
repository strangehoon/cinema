output "app_url" {
  value = "http://${aws_eip.app.public_ip}"
}

output "instance_ids" {
  value = { for role, instance in aws_instance.node : role => instance.id }
}

output "github_deploy_role_arn" {
  value = aws_iam_role.github_deploy.arn
}
