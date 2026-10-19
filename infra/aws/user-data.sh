#!/bin/bash
set -euo pipefail

cat >/etc/systemd/system/cinema-auto-stop.service <<'UNIT'
[Unit]
Description=Stop Cinema EC2 four hours after each boot
After=network-online.target

[Service]
Type=oneshot
ExecStart=/usr/sbin/shutdown -h +240
RemainAfterExit=yes

[Install]
WantedBy=multi-user.target
UNIT
systemctl daemon-reload
systemctl enable --now cinema-auto-stop.service

dnf install -y docker
systemctl enable --now docker
systemctl enable --now amazon-ssm-agent

mkdir -p /usr/local/lib/docker/cli-plugins /opt/cinema
curl -fsSL https://github.com/docker/compose/releases/download/v5.6.0/docker-compose-linux-x86_64 \
  -o /usr/local/lib/docker/cli-plugins/docker-compose
chmod +x /usr/local/lib/docker/cli-plugins/docker-compose
docker compose version
