data "aws_ssm_parameter" "al2023" {
  name = "/aws/service/ami-amazon-linux-latest/al2023-ami-kernel-default-x86_64"
}

locals {
  nodes = {
    app        = { type = "t3.small", ip = "10.20.1.10", disk = 20 }
    mysql      = { type = "t3.small", ip = "10.20.1.20", disk = 30 }
    redis      = { type = "t3.micro", ip = "10.20.1.30", disk = 16 }
    monitoring = { type = "t3.small", ip = "10.20.1.40", disk = 20 }
  }
}

resource "random_password" "mysql_root" {
  length  = 32
  special = false
}

resource "random_password" "mysql_user" {
  length  = 32
  special = false
}

resource "random_password" "grafana_admin" {
  length  = 32
  special = false
}

resource "random_password" "n8n_encryption_key" {
  length  = 64
  special = false
}

resource "aws_ssm_parameter" "mysql_root" {
  name  = "/cinema/mysql-root-password"
  type  = "SecureString"
  value = random_password.mysql_root.result
}

resource "aws_ssm_parameter" "mysql_user" {
  name  = "/cinema/mysql-password"
  type  = "SecureString"
  value = random_password.mysql_user.result
}

resource "aws_ssm_parameter" "grafana_admin" {
  name  = "/cinema/grafana-admin-password"
  type  = "SecureString"
  value = random_password.grafana_admin.result
}

resource "aws_ssm_parameter" "n8n_encryption_key" {
  name  = "/cinema/n8n-encryption-key"
  type  = "SecureString"
  value = random_password.n8n_encryption_key.result
}

locals {
  node_parameter_arns = {
    app        = [aws_ssm_parameter.mysql_user.arn]
    mysql      = [aws_ssm_parameter.mysql_user.arn, aws_ssm_parameter.mysql_root.arn]
    redis      = []
    monitoring = [aws_ssm_parameter.grafana_admin.arn, aws_ssm_parameter.n8n_encryption_key.arn]
  }
}

resource "aws_iam_role" "node" {
  for_each = local.node_parameter_arns
  name     = "cinema-${each.key}-ec2"
  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect    = "Allow"
      Principal = { Service = "ec2.amazonaws.com" }
      Action    = "sts:AssumeRole"
    }]
  })
}

resource "aws_iam_role_policy_attachment" "ssm" {
  for_each   = aws_iam_role.node
  role       = each.value.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore"
}

resource "aws_iam_role_policy" "node" {
  for_each = { for role_name, arns in local.node_parameter_arns : role_name => arns if length(arns) > 0 }
  name     = "cinema-${each.key}-parameter-read"
  role     = aws_iam_role.node[each.key].id
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = ["ssm:GetParameter", "ssm:GetParameters"]
        Resource = each.value
      }
    ]
  })
}

resource "aws_iam_instance_profile" "node" {
  for_each = aws_iam_role.node
  name     = "cinema-${each.key}-ec2"
  role     = each.value.name
}

resource "aws_instance" "node" {
  for_each                             = local.nodes
  ami                                  = data.aws_ssm_parameter.al2023.value
  instance_type                        = each.value.type
  subnet_id                            = aws_subnet.public.id
  private_ip                           = each.value.ip
  associate_public_ip_address          = true
  vpc_security_group_ids               = [aws_security_group.node[each.key].id]
  iam_instance_profile                 = aws_iam_instance_profile.node[each.key].name
  instance_initiated_shutdown_behavior = "stop"
  user_data                            = file("${path.module}/user-data.sh")
  user_data_replace_on_change          = false

  root_block_device {
    volume_type           = "gp3"
    volume_size           = each.value.disk
    encrypted             = true
    delete_on_termination = true
  }

  tags = { Name = "cinema-${each.key}", Role = each.key }
}

resource "aws_eip" "app" {
  domain   = "vpc"
  instance = aws_instance.node["app"].id
  tags     = { Name = "cinema-app" }
}

resource "aws_budgets_budget" "monthly" {
  name         = "cinema-monthly"
  budget_type  = "COST"
  limit_amount = tostring(var.monthly_budget_usd)
  limit_unit   = "USD"
  time_unit    = "MONTHLY"

  notification {
    comparison_operator        = "GREATER_THAN"
    threshold                  = 80
    threshold_type             = "PERCENTAGE"
    notification_type          = "ACTUAL"
    subscriber_email_addresses = [var.budget_email]
  }

  notification {
    comparison_operator        = "GREATER_THAN"
    threshold                  = 100
    threshold_type             = "PERCENTAGE"
    notification_type          = "ACTUAL"
    subscriber_email_addresses = [var.budget_email]
  }
}
