# Cinema

로컬에서 실행하는 영화 예매 서비스입니다. React·TypeScript 화면, Spring Boot API, MySQL, Redis를 사용합니다. 좌석 예매의 동시성 제어는 이번 구현 범위에서 제외했습니다.

## 실행

Docker Desktop을 켜고 프로젝트 루트에서 실행합니다.

```sh
docker compose up --build
```

웹 주소는 http://localhost:8080 입니다. 첫 실행에서는 이미지를 내려받고 12편의 영화, 3개 상영관, 상영 일정과 좌석을 생성하므로 시간이 걸릴 수 있습니다. DB는 전용 Docker 볼륨에 저장됩니다.

체험 계정은 `demo@cinema.local` / `cinema1234`입니다. 새 계정을 가입해 사용해도 됩니다. 기본 결제는 실제 금액이 청구되지 않는 모의 결제입니다.

토스 테스트 카드 결제를 사용하려면 프로젝트 루트의 `.env`에 `PAYMENT_MODE=toss`, `TOSS_CLIENT_KEY=...`, `TOSS_SECRET_KEY=...`를 설정한 뒤 다시 `docker compose up --build`를 실행합니다. 토스 테스트 키를 사용해야 하며 비밀 키는 브라우저에 전달하지 않습니다. 결제 승인·취소는 서버에서 처리합니다. 결제 대기 시간은 5분이고, 실패한 결제는 만료 전까지 다시 시도할 수 있습니다.

## 검증

```sh
./gradlew test
cd web && npm ci && npm run build && npm run test:e2e
```

실제 Docker 서비스의 가입→예매→결제→취소와 모바일 화면은 웹 디렉터리에서 다음 명령으로 확인합니다.

```sh
LIVE_BASE_URL=http://localhost:8080 npx playwright test live.spec.ts
```

Windows PowerShell에서는 `$env:LIVE_BASE_URL='http://localhost:8080'; npx playwright test live.spec.ts`를 사용합니다. 최초에는 `npx playwright install chromium`이 필요합니다.

영화와 상영 일정은 자동으로 생성되고, 관리자 변경 API는 없습니다. 회차의 잔여 좌석은 좌석 상태에서 집계하며, 예약 후 5분 또는 상영 시작 시각 중 빠른 때에 미결제 좌석을 해제합니다.
