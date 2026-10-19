resource "aws_iam_openid_connect_provider" "github" {
  url            = "https://token.actions.githubusercontent.com"
  client_id_list = ["sts.amazonaws.com"]
}

resource "aws_iam_role" "github_deploy" {
  name = "cinema-github-deploy"
  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect    = "Allow"
      Principal = { Federated = aws_iam_openid_connect_provider.github.arn }
      Action    = "sts:AssumeRoleWithWebIdentity"
      Condition = {
        StringEquals = {
          "token.actions.githubusercontent.com:aud" = "sts.amazonaws.com"
          "token.actions.githubusercontent.com:sub" = "repo:${var.github_repository}:ref:refs/heads/develop"
        }
      }
    }]
  })
}

resource "aws_iam_role_policy" "github_deploy" {
  name = "cinema-deploy"
  role = aws_iam_role.github_deploy.id
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect   = "Allow"
        Action   = ["ec2:DescribeInstances", "ec2:DescribeInstanceStatus", "ssm:DescribeInstanceInformation", "ssm:GetCommandInvocation"]
        Resource = "*"
      },
      {
        Effect   = "Allow"
        Action   = "ec2:StartInstances"
        Resource = [for instance in aws_instance.node : instance.arn]
      },
      {
        Effect = "Allow"
        Action = "ssm:SendCommand"
        Resource = concat(
          ["arn:aws:ssm:${var.region}::document/AWS-RunShellScript"],
          [for instance in aws_instance.node : instance.arn]
        )
      },
      {
        Effect   = "Allow"
        Action   = "s3:PutObject"
        Resource = "${aws_s3_bucket.artifacts.arn}/releases/*"
      }
    ]
  })
}
