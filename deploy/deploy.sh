#!/bin/bash

IMAGE=$1
if [ -z "$IMAGE" ]; then
  echo "❌ 이미지 이름이 필요합니다. 예: ./deploy.sh username/image"
  exit 1
fi

echo "🚀 docker-compose 기반 배포 시작 - 이미지: $IMAGE"

# 현재 컨테이너 상태로 포트 판단
if docker ps --format '{{.Names}}' | grep -q app-blue; then
  CURRENT_PORT=8081
elif docker ps --format '{{.Names}}' | grep -q app-green; then
  CURRENT_PORT=8082
else
  echo "⚠️ 컨테이너가 아무것도 떠있지 않습니다. 초기 배포로 간주합니다."
  CURRENT_PORT=8082
fi

# 다음 포트/스크립트 결정
if [ "$CURRENT_PORT" -eq 8081 ]; then
  NEXT_PORT=8082
  NEXT_NAME=app-green
  OLD_NAME=app-blue
  COMPOSE_FILE="docker-compose.green.yml"
  NGINX_SCRIPT=~/scripts/nginx_green.sh
else
  NEXT_PORT=8081
  NEXT_NAME=app-blue
  OLD_NAME=app-green
  COMPOSE_FILE="docker-compose.blue.yml"
  NGINX_SCRIPT=~/scripts/nginx_blue.sh
fi

echo "현재 포트: $CURRENT_PORT → 새 포트: $NEXT_PORT"
echo "새 컨테이너: $NEXT_NAME | 이전 컨테이너: $OLD_NAME"

# 템플릿 compose 파일 복사 → 이미지 이름 치환
cp "$COMPOSE_FILE" temp-compose.yml
sed -i "s|YOUR_IMAGE_NAME|$IMAGE|" temp-compose.yml

# 기존 컨테이너 중지 및 제거
docker compose -f temp-compose.yml down > /dev/null 2>&1 || true

# 새 컨테이너 실행
docker compose -f temp-compose.yml up -d
echo "🟡 새 컨테이너 실행됨 → 헬스체크 시작..."

# 헬스체크
for i in {1..60}; do
  sleep 2
  STATUS_CODE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:$NEXT_PORT/actuator/health)
  echo "🔎 응답 코드: $STATUS_CODE"
  if [ "$STATUS_CODE" -eq 200 ]; then
    echo "✅ 헬스체크 통과"
    break
  fi
  if [ $i -eq 10 ]; then
    echo "❌ 헬스체크 실패. 새 컨테이너 중단"
    echo "📦 실패한 컨테이너 로그 출력 ($NEXT_NAME)"
    docker logs $NEXT_NAME || echo "⚠️ 로그를 가져올 수 없습니다."
    docker compose -f temp-compose.yml down
    rm temp-compose.yml
    exit 1
  fi
done

# Nginx 전환
echo "🔁 Nginx 포트 전환: $NGINX_SCRIPT"
bash $NGINX_SCRIPT

# 이전 컨테이너 제거
docker rm -f $OLD_NAME > /dev/null 2>&1 || true

# 임시 파일 제거
rm temp-compose.yml

echo "🎉 배포 완료: 현재 실행 중인 컨테이너 → $NEXT_NAME (포트 $NEXT_PORT)"