#!/bin/bash
set -euo pipefail

root="$(cd "$(dirname "$0")/.." && pwd)"
temp_base="$(cd "${TMPDIR:-/tmp}" && pwd -P)"
temporary="$(mktemp -d "$temp_base/cinema-deploy-test.XXXXXX")"
cleanup() {
  resolved="$(cd "$temporary" && pwd -P)"
  [[ "$resolved" == "$temp_base"/cinema-deploy-test.* ]] || return 1
  rm -rf -- "$resolved"
}
trap cleanup EXIT
mkdir -p "$temporary/bin"

cat >"$temporary/bin/aws" <<'MOCK'
#!/bin/bash
echo testpassword
MOCK
cat >"$temporary/bin/docker" <<'MOCK'
#!/bin/bash
echo "$*" >>"$DOCKER_LOG"
if [[ "$*" == "compose ps --status running proxy" ]]; then echo proxy; fi
MOCK
cat >"$temporary/bin/curl" <<'MOCK'
#!/bin/bash
if [[ "${FAIL_SMOKE:-0}" == 1 && "$*" == *api/movies* ]]; then exit 22; fi
MOCK
chmod +x "$temporary/bin/"*
export PATH="$temporary/bin:$PATH"

prepare() {
  export CINEMA_HOME="$temporary/$1"
  export DOCKER_LOG="$CINEMA_HOME/docker.log"
  mkdir -p "$CINEMA_HOME/release/app" "$CINEMA_HOME/app/nginx"
  cp "$root/app/compose.yml" "$root/app/nginx.conf.template" "$CINEMA_HOME/release/app/"
  cp "$root/app/compose.yml" "$root/app/nginx.conf.template" "$CINEMA_HOME/app/"
  echo blue >"$CINEMA_HOME/app/active-color"
  sed 's/__COLOR__/blue/g' "$root/app/nginx.conf.template" >"$CINEMA_HOME/app/nginx/default.conf"
  cat >"$CINEMA_HOME/app/.env" <<'ENV'
DB_PASSWORD=testpassword
API_BLUE_IMAGE=example/cinema:api-old
WEB_BLUE_IMAGE=example/cinema:web-old
API_GREEN_IMAGE=example/cinema:api-older
WEB_GREEN_IMAGE=example/cinema:web-older
ENV
}

sha=1111111111111111111111111111111111111111
prepare success
bash "$root/app/deploy.sh" "$sha" example/cinema
[[ "$(cat "$CINEMA_HOME/app/active-color")" == green ]]
grep -q 'api-green:8081' "$CINEMA_HOME/app/nginx/default.conf"
grep -q 'stop api-blue web-blue' "$DOCKER_LOG"

prepare failure
export FAIL_SMOKE=1
if bash "$root/app/deploy.sh" "$sha" example/cinema; then
  echo "Expected smoke-test failure" >&2
  exit 1
fi
[[ "$(cat "$CINEMA_HOME/app/active-color")" == blue ]]
grep -q 'api-blue:8081' "$CINEMA_HOME/app/nginx/default.conf"
grep -q 'stop api-green web-green' "$DOCKER_LOG"

unset FAIL_SMOKE
prepare manual_rollback
bash "$root/app/rollback.sh"
[[ "$(cat "$CINEMA_HOME/app/active-color")" == green ]]
grep -q 'api-green:8081' "$CINEMA_HOME/app/nginx/default.conf"
grep -q 'stop api-blue web-blue' "$DOCKER_LOG"

echo "Deployment switch and rollback tests passed"
