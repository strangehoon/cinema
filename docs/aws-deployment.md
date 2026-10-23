# AWS 배포 운영

이 문서는 이슈 #25의 배포 절차다. 로컬 `docker compose up --build`와 루트 README는 그대로 유지한다.

## 구성

- 리전 `ap-northeast-2`, 단일 가용 영역, 앱·MySQL·모니터링 `t3.small`과 Redis `t3.micro` 각 1대.
- 각 EC2의 서비스는 Docker Compose로 실행한다. 앱 EC2는 Nginx가 Blue/Green 웹·API 중 활성 색상으로 요청을 전달한다. 모니터링 EC2에는 Prometheus, Grafana, Alertmanager, Loki, n8n을 실행하고 앱 EC2의 Alloy가 API 컨테이너 로그를 Loki로 보낸다.
- 앱의 80번 포트만 공개한다. DB·Redis·메트릭은 보안 그룹의 내부 통신으로 제한하고 Grafana는 SSM 포트 포워딩으로 본다.
- MySQL Docker 볼륨은 루트 EBS에 저장된다. EC2 중지·재시작 후 유지되지만 인스턴스 교체나 디스크 장애 시 복구하지 않는다. Redis 캐시는 재시작 시 사라진다.
- 부팅마다 4시간 후 인스턴스 자체 종료를 예약하며 EC2의 종료 동작은 `stop`이다. 따라서 평소 중지 상태이고 `develop` 배포가 시작되면 네 인스턴스를 다시 켠다.

## 최초 준비

1. AWS CLI v2, Terraform 1.10 이상, Docker를 준비한다. `aws configure sso --profile cinema`와 `aws sso login --profile cinema`로 배포할 AWS 계정에 로그인한다. `aws sts get-caller-identity`로 계정을 확인한다.
2. 예상 요금을 AWS Pricing Calculator에서 검토한다. Terraform의 `monthly_budget_usd` 기본값은 65 USD이며, 적용 시점의 환율로 월 10만 원 목표와 비교해 조정한다. AWS Budget 알림은 지출을 강제로 차단하지 않는다. EBS와 앱 Elastic IP는 인스턴스가 중지되어도 과금될 수 있다.
3. Terraform 상태용 S3 버킷을 계정당 한 번 만든다. 버킷은 버전 관리, 기본 암호화, 공개 접근 차단을 활성화한다. 상태 파일에는 생성한 DB·Grafana 비밀번호가 포함될 수 있으므로 접근 권한을 제한한다.

```powershell
$env:AWS_PROFILE = 'cinema'
$accountId = aws sts get-caller-identity --query Account --output text
$stateBucket = "cinema-tfstate-$accountId-ap-northeast-2"
aws s3api create-bucket --bucket $stateBucket --region ap-northeast-2 --create-bucket-configuration LocationConstraint=ap-northeast-2
aws s3api put-bucket-versioning --bucket $stateBucket --versioning-configuration Status=Enabled
aws s3api put-public-access-block --bucket $stateBucket --public-access-block-configuration BlockPublicAcls=true,IgnorePublicAcls=true,BlockPublicPolicy=true,RestrictPublicBuckets=true
aws s3api put-bucket-encryption --bucket $stateBucket --server-side-encryption-configuration '{"Rules":[{"ApplyServerSideEncryptionByDefault":{"SSEAlgorithm":"AES256"}}]}'
cd infra/aws
terraform init -backend-config="bucket=$stateBucket"
terraform validate
terraform plan -var='budget_email=본인@example.com'
terraform apply -var='budget_email=본인@example.com'
```

4. Terraform 출력의 `github_deploy_role_arn`, `app_url`을 확인한다. GitHub 저장소의 Actions **Variables**에 `AWS_ROLE_ARN`을 설정한다. Actions **Secrets**인 `DOCKERHUB_USERNAME`, `DOCKERHUB_TOKEN`, `DOCKERHUB_REPONAME`도 확인한다. EC2가 이미지와 해당 커밋의 배포 파일을 읽도록 Docker Hub와 GitHub 저장소는 공개로 설정한다.
5. `develop` 푸시 때 테스트 → API·웹 이미지를 Docker Hub에 게시 → EC2 기동 → 각 EC2가 해당 커밋의 GitHub 소스 아카이브에서 배포 파일을 다운로드 → MySQL·Redis·모니터링 시작 → 비활성 색상 배포·검사 → Nginx 전환 순서로 진행된다. GitHub Actions는 OIDC 역할로 AWS SSM 명령을 보낸다. 배포 아티팩트용 S3는 사용하지 않으며 Terraform 상태용 S3는 계속 사용한다.

## 확인과 복구

- 앱 주소는 `terraform output -raw app_url`이다. 첫 실행 시 샘플 영화와 상영 일정이 채워질 때까지 시간이 걸릴 수 있다. 공개 HTTP에서는 실제 비밀번호나 개인정보 대신 체험 계정만 사용한다.
- GitHub Actions의 `deploy` 작업에서 각 서버의 SSM 명령 결과를 확인한다. 초기 배포 실패 시 앱 주소는 아직 열리지 않으며, 이후 배포 실패 시 Nginx는 이전 색상을 유지한다.
- 성공 후 문제가 발견되면 SSM Run Command로 앱 EC2에서 `bash /opt/cinema/release/app/rollback.sh`를 실행한다. 이전 색상의 이미지와 DB 스키마가 새 버전과 호환되어야 한다. DB 마이그레이션은 Blue/Green 공존과 롤백을 고려해 이전 API와도 호환되게 작성한다.
- Grafana는 SSM Session Manager의 `AWS-StartPortForwardingSession`으로 모니터링 EC2의 3000번 포트를 로컬에 전달해 `http://localhost:3000`에서 본다. 관리자 비밀번호는 Parameter Store의 `/cinema/grafana-admin-password`에 있다.
- n8n 편집기는 같은 방식으로 모니터링 EC2의 5678번 포트를 로컬로 전달해 `http://localhost:5678`에서 연다. 포트는 EC2의 loopback에만 바인딩되어 보안 그룹에서 공개하지 않는다. n8n 암호화 키는 최초 설치 시 생성해 모니터링 EC2의 권한 제한된 `.env`에 보관하고 재배포 때 재사용한다.
- 필요할 때 EC2를 수동 기동할 수 있다. 각 인스턴스는 부팅 시 4시간 자동 중지를 다시 예약한다. DB가 중지된 동안 API 서비스도 이용할 수 없다.
- 기존 `AWS_ARTIFACT_BUCKET` Actions 변수를 삭제한다. Terraform 변경을 적용하기 전에 배포용 `cinema-artifacts-<계정 ID>-ap-northeast-2` 버킷의 객체를 비워야 버킷 삭제가 가능하다. 상태용 `cinema-tfstate-<계정 ID>-ap-northeast-2` 버킷은 삭제하지 않는다.

## 범위와 제약

- 로그인은 현재 서버 세션 방식을 유지한다. Blue/Green 전환으로 로그인 상태가 끊길 수 있다.
- MySQL 기반 ShedLock은 정기 작업과 시작 시 샘플 데이터 생성의 중복 실행을 막는다. 좌석 예매는 별도로 MySQL의 좌석 재고 행과 사용자 행에 비관적 쓰기 락을 적용한다.
- HTTPS·도메인, DB 백업·교체 복구, k6 부하 테스트는 이번 배포에 포함하지 않는다.

## 모니터링 및 AI 장애 분석

### 데이터 흐름과 보존

- Prometheus는 API·EC2·MySQL·Redis 지표를 수집하고 7일 또는 5GB까지 보관한다. Alertmanager는 장애 알림을 그룹화해 내부 n8n webhook으로 전달한다.
- Alloy는 앱 EC2의 Docker JSON 로그 파일을 읽어 모니터링 EC2의 Loki에 보낸다. Docker 로그는 컨테이너마다 10MB 파일 3개로 회전하고 Loki는 로그를 7일 보관한다.
- Grafana는 Prometheus, Loki, Alertmanager를 데이터 소스로 사용한다. 기존 대시보드는 Grafana 볼륨에 보존된다.
- n8n은 SQLite 데이터와 암호화된 자격 증명을 Docker 볼륨에 보관한다. 암호화 키는 모니터링 EC2의 `/opt/cinema/monitoring/.env`에 보관하며 재배포 때 바뀌지 않는다. 실행 성공·실패 본문은 SQLite에 남기지 않도록 설정한다.
- 모니터링 EC2는 `t3.small`, 20GB를 유지한다. Compose의 메모리 상한 합은 약 1.5GB이고 Alloy는 앱 EC2에서 100MB로 제한한다. Loki·Prometheus·n8n 데이터는 해당 EC2 Docker 볼륨에만 남아 인스턴스 교체 시 복구하지 않는다.

### 최초 n8n 설정

1. 모니터링 EC2가 SSM Online인지 확인한 뒤 5678번 포트를 포워딩한다.

```powershell
aws ssm start-session --profile cinema --region ap-northeast-2 --target <monitoring-instance-id> --document-name AWS-StartPortForwardingSession --parameters 'portNumber=5678,localPortNumber=5678'
```

2. `http://localhost:5678`에서 n8n 소유자 계정을 만든다. 이 편집기는 SSM 터널로만 접근한다.
3. `deploy/aws/monitoring/workflows/cinema-ai-alerts.json`을 가져온다. HTTP Header Auth 자격 증명을 두 개 만들고 해당 HTTP Request 노드에 연결한다.
   - Slack 자격 증명: 헤더 이름 `Authorization`, 값 `Bearer <Slack bot token>`; 봇에 `chat:write` 권한을 주고 알림 채널에 초대한 뒤 채널 ID를 확인한다.
   - OpenAI 자격 증명: 헤더 이름 `Authorization`, 값 `Bearer <OpenAI API key>`.
4. 워크플로의 `Slack Root Alert` 노드에서 `CINEMA_ALERT_CHANNEL_ID`를 실제 채널 ID로 바꾼다. `Slack Root Alert`와 `Slack AI Thread Reply`에 Slack 자격 증명을 연결하고, `OpenAI Incident Analysis`에는 OpenAI 자격 증명을 연결한다.
5. 워크플로를 저장하고 활성화한다. Grafana/Alertmanager가 보내는 root 장애 알림이 먼저 Slack에 올라오고, firing 알림에 대해서만 분석 결과가 같은 스레드에 이어진다. resolved 알림은 root 해소 알림만 보낸다.

워크플로는 가장 최근 ERROR 로그에서 `com.example` Java stack frame과 40자리 배포 SHA를 찾을 때만 해당 커밋의 공개 GitHub 원본 파일을 읽는다. 주변 코드 최대 80줄과 제한된 로그·지표만 OpenAI에 전달한다. 이메일·전화번호·토큰·비밀번호처럼 식별되는 패턴은 전송 전에 가린다. 로그나 SHA가 없으면 코드 관련 원인을 추측하지 않는다. 분석은 후보와 확인 순서를 제안할 뿐 자동 조치하지 않는다. Slack 최초 알림이 실패하면 분석을 진행하지 않는다. OpenAI 요청이 실패해도 최초 Slack 알림은 남고 스레드에 실패 안내를 붙인다.

현재 임계값은 API 대상 scrape 실패 1분, 5xx 비율 5% 초과가 5분 지속, EC2 CPU 80%·메모리 85%·루트 디스크 80% 초과가 5분 지속, MySQL/Redis exporter 실패 1분, API ERROR 로그 5분 내 5건 이상이다. Grafana/Prometheus의 Alerting 화면과 Slack 테스트 알림으로 최초 연결을 확인한다.
