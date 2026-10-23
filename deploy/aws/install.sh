#!/bin/bash
set -euo pipefail

role="$1"
sha="$2"
image_base="${3:-}"
export AWS_DEFAULT_REGION=ap-northeast-2

mkdir -p /opt/cinema/common
cp /opt/cinema/release/common/compose.yml /opt/cinema/common/compose.yml
docker compose -f /opt/cinema/common/compose.yml up -d

case "$role" in
  mysql)
    mkdir -p /opt/cinema/mysql
    cp /opt/cinema/release/mysql/compose.yml /opt/cinema/mysql/compose.yml
    password="$(aws ssm get-parameter --name /cinema/mysql-password --with-decryption --query Parameter.Value --output text)"
    root_password="$(aws ssm get-parameter --name /cinema/mysql-root-password --with-decryption --query Parameter.Value --output text)"
    printf 'MYSQL_PASSWORD=%s\nMYSQL_ROOT_PASSWORD=%s\n' "$password" "$root_password" >/opt/cinema/mysql/.env
    chmod 600 /opt/cinema/mysql/.env
    cd /opt/cinema/mysql
    docker compose up -d --wait --wait-timeout 300
    ;;
  redis)
    mkdir -p /opt/cinema/redis
    cp /opt/cinema/release/redis/compose.yml /opt/cinema/redis/compose.yml
    cd /opt/cinema/redis
    docker compose up -d --wait --wait-timeout 180
    ;;
  monitoring)
    mkdir -p /opt/cinema/monitoring
    cp /opt/cinema/release/monitoring/compose.yml /opt/cinema/monitoring/compose.yml
    cp /opt/cinema/release/monitoring/prometheus.yml /opt/cinema/monitoring/prometheus.yml
    cp /opt/cinema/release/monitoring/alert-rules.yml /opt/cinema/monitoring/alert-rules.yml
    cp /opt/cinema/release/monitoring/alertmanager.yml /opt/cinema/monitoring/alertmanager.yml
    cp /opt/cinema/release/monitoring/loki.yml /opt/cinema/monitoring/loki.yml
    cp /opt/cinema/release/monitoring/alerting.yml /opt/cinema/monitoring/alerting.yml
    cp /opt/cinema/release/monitoring/datasource.yml /opt/cinema/monitoring/datasource.yml
    mkdir -p /opt/cinema/monitoring/loki-rules/fake /opt/cinema/monitoring/workflows
    cp /opt/cinema/release/monitoring/loki-rules/fake/cinema-alerts.yml /opt/cinema/monitoring/loki-rules/fake/cinema-alerts.yml
    cp /opt/cinema/release/monitoring/workflows/cinema-ai-alerts.json /opt/cinema/monitoring/workflows/cinema-ai-alerts.json
    password="$(aws ssm get-parameter --name /cinema/grafana-admin-password --with-decryption --query Parameter.Value --output text)"
    n8n_encryption_key=""
    if [[ -f /opt/cinema/monitoring/.env ]]; then
      n8n_encryption_key="$(sed -n 's/^N8N_ENCRYPTION_KEY=//p' /opt/cinema/monitoring/.env | head -n 1)"
    fi
    if [[ -z "$n8n_encryption_key" ]]; then
      n8n_encryption_key="$(openssl rand -hex 32)"
    fi
    printf 'GRAFANA_ADMIN_PASSWORD=%s\nN8N_ENCRYPTION_KEY=%s\n' "$password" "$n8n_encryption_key" >/opt/cinema/monitoring/.env
    chmod 600 /opt/cinema/monitoring/.env
    cd /opt/cinema/monitoring
    docker compose up -d --wait --wait-timeout 180
    ;;
  app)
    if [[ -z "$image_base" ]]; then echo "Docker image base is required" >&2; exit 1; fi
    bash /opt/cinema/release/app/deploy.sh "$sha" "$image_base"
    ;;
  *)
    echo "Unknown Cinema role: $role" >&2
    exit 1
    ;;
esac
