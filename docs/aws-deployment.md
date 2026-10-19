# AWS 배포 운영

이 문서는 이슈 #25의 배포 절차다. 로컬 `docker compose up --build`와 루트 README는 그대로 유지한다.

## 구성

- 리전 `ap-northeast-2`, 단일 가용 영역, 앱·MySQL·모니터링 `t3.small`과 Redis `t3.micro` 각 1대.
- 각 EC2의 서비스는 Docker Compose로 실행한다. 앱 EC2는 Nginx가 Blue/Green 웹·API 중 활성 색상으로 요청을 전달한다.
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

4. Terraform 출력의 `github_deploy_role_arn`, `artifact_bucket`, `app_url`을 확인한다. GitHub 저장소의 Actions **Variables**에 `AWS_ROLE_ARN`과 `AWS_ARTIFACT_BUCKET`을 각각 설정한다. 기존 Actions **Secrets**인 `DOCKERHUB_USERNAME`, `DOCKERHUB_TOKEN`, `DOCKERHUB_REPONAME`도 확인한다. EC2가 이미지를 읽도록 Docker Hub 저장소는 공개로 설정한다.
5. `feature/#25` PR의 CI가 통과한 뒤 `develop`에 병합한다. 이후 `develop` 푸시 때 테스트 → API·웹 이미지 게시 → EC2 기동 → MySQL·Redis·모니터링 시작 → 비활성 색상 배포·검사 → Nginx 전환 순서로 진행된다. GitHub Actions가 AWS에 접근할 때에는 OIDC 역할을 사용한다.

## 확인과 복구

- 앱 주소는 `terraform output -raw app_url`이다. 첫 실행 시 샘플 영화와 상영 일정이 채워질 때까지 시간이 걸릴 수 있다. 공개 HTTP에서는 실제 비밀번호나 개인정보 대신 체험 계정만 사용한다.
- GitHub Actions의 `deploy` 작업에서 각 서버의 SSM 명령 결과를 확인한다. 초기 배포 실패 시 앱 주소는 아직 열리지 않으며, 이후 배포 실패 시 Nginx는 이전 색상을 유지한다.
- 성공 후 문제가 발견되면 SSM Run Command로 앱 EC2에서 `bash /opt/cinema/release/app/rollback.sh`를 실행한다. 이전 색상의 이미지와 DB 스키마가 새 버전과 호환되어야 한다. DB 마이그레이션은 Blue/Green 공존과 롤백을 고려해 이전 API와도 호환되게 작성한다.
- Grafana는 SSM Session Manager의 `AWS-StartPortForwardingSession`으로 모니터링 EC2의 3000번 포트를 로컬에 전달해 `http://localhost:3000`에서 본다. 관리자 비밀번호는 Parameter Store의 `/cinema/grafana-admin-password`에 있다.
- 필요할 때 EC2를 수동 기동할 수 있다. 각 인스턴스는 부팅 시 4시간 자동 중지를 다시 예약한다. DB가 중지된 동안 API 서비스도 이용할 수 없다.

## 범위와 제약

- 로그인은 현재 서버 세션 방식을 유지한다. Blue/Green 전환으로 로그인 상태가 끊길 수 있다.
- MySQL 기반 ShedLock은 정기 작업과 시작 시 샘플 데이터 생성의 중복 실행만 막는다. 좌석 예매 요청의 동시성 제어에는 사용하지 않는다.
- HTTPS·도메인, DB 백업·교체 복구, k6 부하 테스트는 이번 배포에 포함하지 않는다.
