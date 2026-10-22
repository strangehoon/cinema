#!/bin/bash
set -euo pipefail

sha="$1"
image_base="$2"
[[ "$sha" =~ ^[0-9a-f]{40}$ ]] || { echo "Invalid commit SHA" >&2; exit 1; }
[[ "$image_base" =~ ^[a-zA-Z0-9._/-]+$ ]] || { echo "Invalid Docker image name" >&2; exit 1; }

cinema_home="${CINEMA_HOME:-/opt/cinema}"
app_dir="$cinema_home/app"
mkdir -p "$app_dir/nginx"
cp "$cinema_home/release/app/compose.yml" "$app_dir/compose.yml"
cp "$cinema_home/release/app/nginx.conf.template" "$app_dir/nginx.conf.template"
cp "$cinema_home/release/app/web.conf" "$app_dir/web.conf"
cp "$cinema_home/release/app/alloy.alloy" "$app_dir/alloy.alloy"
cd "$app_dir"

docker compose up -d alloy

active=""
if [[ -f active-color ]]; then active="$(cat active-color)"; fi
if [[ "$active" == blue ]]; then target=green; else target=blue; fi

password="$(aws ssm get-parameter --name /cinema/mysql-password --with-decryption --query Parameter.Value --output text)"
chmod 700 "$app_dir"
if [[ -f .env ]]; then
  # Docker image names are validated before they are persisted in this file.
  source .env
fi
API_BLUE_IMAGE="${API_BLUE_IMAGE:-${image_base}:api-${sha}}"
WEB_BLUE_IMAGE="${WEB_BLUE_IMAGE:-${image_base}:web-${sha}}"
API_GREEN_IMAGE="${API_GREEN_IMAGE:-${image_base}:api-${sha}}"
WEB_GREEN_IMAGE="${WEB_GREEN_IMAGE:-${image_base}:web-${sha}}"
if [[ "$target" == blue ]]; then
  API_BLUE_IMAGE="${image_base}:api-${sha}"
  WEB_BLUE_IMAGE="${image_base}:web-${sha}"
else
  API_GREEN_IMAGE="${image_base}:api-${sha}"
  WEB_GREEN_IMAGE="${image_base}:web-${sha}"
fi
cat >.env <<EOF
DB_PASSWORD=$password
APP_VERSION=$sha
API_BLUE_IMAGE=$API_BLUE_IMAGE
WEB_BLUE_IMAGE=$WEB_BLUE_IMAGE
API_GREEN_IMAGE=$API_GREEN_IMAGE
WEB_GREEN_IMAGE=$WEB_GREEN_IMAGE
EOF
chmod 600 .env

cleanup_candidate() {
  docker compose --profile "$target" stop "api-$target" "web-$target" || true
}

rollback() {
  if [[ -n "$active" ]]; then
    sed "s/__COLOR__/$active/g" nginx.conf.template >nginx/default.conf.next
    mv nginx/default.conf.next nginx/default.conf
    docker compose exec -T proxy nginx -t || true
    docker compose exec -T proxy nginx -s reload || true
  else
    docker compose stop proxy || true
  fi
  cleanup_candidate
}

if ! docker compose --profile "$target" pull "api-$target" "web-$target" || \
   ! docker compose --profile "$target" up -d --wait --wait-timeout 600 "api-$target" "web-$target"; then
  cleanup_candidate
  exit 1
fi

sed "s/__COLOR__/$target/g" nginx.conf.template >nginx/default.conf.next
mv nginx/default.conf.next nginx/default.conf
if ! docker compose ps --status running proxy | grep -q proxy; then
  if ! docker compose up -d proxy; then rollback; exit 1; fi
else
  if ! docker compose exec -T proxy nginx -t || \
     ! docker compose exec -T proxy nginx -s reload; then
    rollback
    exit 1
  fi
fi

if ! curl -fsS --retry 5 --retry-delay 2 http://127.0.0.1/ >/dev/null || \
   ! curl -fsS --retry 5 --retry-delay 2 'http://127.0.0.1/api/movies?page=0&size=1' >/dev/null; then
  rollback
  echo "Smoke test failed; previous color retained" >&2
  exit 1
fi

echo "$target" >active-color
if [[ -n "$active" ]]; then
  docker compose --profile "$active" stop "api-$active" "web-$active" || true
fi
echo "Deployed $sha to $target"
