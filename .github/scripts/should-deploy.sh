#!/usr/bin/env bash
set -euo pipefail

while IFS= read -r path; do
  case "$path" in
    Dockerfile|.dockerignore|build.gradle|settings.gradle|gradlew|gradlew.bat|gradle/*|src/main/*|\
    web/Dockerfile|web/nginx.conf|web/index.html|web/tsconfig.json|web/vite.config.ts|\
    web/package.json|web/package-lock.json|web/src/*|web/public/*|\
    deploy/aws/*|infra/aws/*|.github/workflows/*)
      exit 0
      ;;
  esac
done

exit 1
