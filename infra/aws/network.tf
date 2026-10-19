resource "aws_vpc" "cinema" {
  cidr_block           = "10.20.0.0/16"
  enable_dns_support   = true
  enable_dns_hostnames = true
  tags                 = { Name = "cinema" }
}

resource "aws_subnet" "public" {
  vpc_id                  = aws_vpc.cinema.id
  cidr_block              = "10.20.1.0/24"
  availability_zone       = data.aws_availability_zones.available.names[0]
  map_public_ip_on_launch = true
  tags                    = { Name = "cinema-public" }
}

data "aws_availability_zones" "available" {
  state = "available"
}

resource "aws_internet_gateway" "cinema" {
  vpc_id = aws_vpc.cinema.id
  tags   = { Name = "cinema" }
}

resource "aws_route_table" "public" {
  vpc_id = aws_vpc.cinema.id
  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.cinema.id
  }
  tags = { Name = "cinema-public" }
}

resource "aws_route_table_association" "public" {
  subnet_id      = aws_subnet.public.id
  route_table_id = aws_route_table.public.id
}

resource "aws_security_group" "node" {
  for_each    = toset(["app", "mysql", "redis", "monitoring"])
  name        = "cinema-${each.key}"
  description = "Cinema ${each.key}"
  vpc_id      = aws_vpc.cinema.id
  tags        = { Name = "cinema-${each.key}" }
}

resource "aws_vpc_security_group_egress_rule" "outbound" {
  for_each          = aws_security_group.node
  security_group_id = each.value.id
  cidr_ipv4         = "0.0.0.0/0"
  ip_protocol       = "-1"
}

resource "aws_vpc_security_group_ingress_rule" "http" {
  security_group_id = aws_security_group.node["app"].id
  cidr_ipv4         = "0.0.0.0/0"
  from_port         = 80
  to_port           = 80
  ip_protocol       = "tcp"
}

resource "aws_vpc_security_group_ingress_rule" "mysql" {
  security_group_id            = aws_security_group.node["mysql"].id
  referenced_security_group_id = aws_security_group.node["app"].id
  from_port                    = 3306
  to_port                      = 3306
  ip_protocol                  = "tcp"
}

resource "aws_vpc_security_group_ingress_rule" "redis" {
  security_group_id            = aws_security_group.node["redis"].id
  referenced_security_group_id = aws_security_group.node["app"].id
  from_port                    = 6379
  to_port                      = 6379
  ip_protocol                  = "tcp"
}

resource "aws_vpc_security_group_ingress_rule" "app_metrics" {
  security_group_id            = aws_security_group.node["app"].id
  referenced_security_group_id = aws_security_group.node["monitoring"].id
  from_port                    = 9091
  to_port                      = 9091
  ip_protocol                  = "tcp"
}

resource "aws_vpc_security_group_ingress_rule" "node_metrics" {
  for_each                     = aws_security_group.node
  security_group_id            = each.value.id
  referenced_security_group_id = aws_security_group.node["monitoring"].id
  from_port                    = 9100
  to_port                      = 9100
  ip_protocol                  = "tcp"
}

resource "aws_vpc_security_group_ingress_rule" "mysql_metrics" {
  security_group_id            = aws_security_group.node["mysql"].id
  referenced_security_group_id = aws_security_group.node["monitoring"].id
  from_port                    = 9104
  to_port                      = 9104
  ip_protocol                  = "tcp"
}

resource "aws_vpc_security_group_ingress_rule" "redis_metrics" {
  security_group_id            = aws_security_group.node["redis"].id
  referenced_security_group_id = aws_security_group.node["monitoring"].id
  from_port                    = 9121
  to_port                      = 9121
  ip_protocol                  = "tcp"
}
