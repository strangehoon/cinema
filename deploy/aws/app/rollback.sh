#!/bin/bash
set -euo pipefail

cd "${CINEMA_HOME:-/opt/cinema}/app"
active="$(cat active-color)"
if [[ "$active" == blue ]]; then previous=green; else previous=blue; fi

docker compose --profile "$previous" up -d --wait --wait-timeout 600 "api-$previous" "web-$previous"
restore_active() {
  sed "s/__COLOR__/$active/g" nginx.conf.template >nginx/default.conf.next
  mv nginx/default.conf.next nginx/default.conf
  docker compose exec -T proxy nginx -s reload || true
  docker compose --profile "$previous" stop "api-$previous" "web-$previous" || true
}
sed "s/__COLOR__/$previous/g" nginx.conf.template >nginx/default.conf.next
mv nginx/default.conf.next nginx/default.conf
if ! docker compose exec -T proxy nginx -t || \
   ! docker compose exec -T proxy nginx -s reload || \
   ! curl -fsS --retry 5 --retry-delay 2 http://127.0.0.1/ >/dev/null || \
   ! curl -fsS --retry 5 --retry-delay 2 'http://127.0.0.1/api/movies?page=0&size=1' >/dev/null; then
  restore_active
  exit 1
fi
echo "$previous" >active-color
docker compose --profile "$active" stop "api-$active" "web-$active"
echo "Rolled back from $active to $previous"
