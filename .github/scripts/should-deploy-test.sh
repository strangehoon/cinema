#!/usr/bin/env bash
set -euo pipefail

script="$(dirname "$0")/should-deploy.sh"

assert_result() {
  local expected="$1"
  shift
  local actual=1

  if printf '%s\n' "$@" | bash "$script"; then
    actual=0
  fi

  if [[ "$actual" != "$expected" ]]; then
    printf 'Expected deploy result %s for paths: %s\n' "$expected" "$*" >&2
    exit 1
  fi
}

assert_result 1 README.md docs/aws-deployment.md docs/images/aws-architecture.png web/tests/booking.spec.ts src/test/java/example/Test.java
assert_result 0 src/main/java/example/ApiApplication.java
assert_result 0 web/src/App.tsx
assert_result 0 deploy/aws/app/deploy.sh
assert_result 0 infra/aws/main.tf
assert_result 0 .github/workflows/ci.yml
assert_result 0 Dockerfile

printf 'Deployment path filter tests passed.\n'
