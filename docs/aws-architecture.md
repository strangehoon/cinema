# AWS 배포 아키텍처

아래 그림은 EC2가 실행 중일 때의 구성이다. 리전은 서울(`ap-northeast-2`)이며, 외부 서비스 주소는 `terraform output -raw app_url`로 확인한다.

```mermaid
flowchart LR
    User[사용자 브라우저] -->|HTTP 80| Proxy

    subgraph AWS[AWS · 서울 리전]
        subgraph App[앱 EC2 · t3.small]
            Proxy[Nginx 프록시]
            Blue[Blue · React 웹 + Spring Boot API]
            Green[Green · React 웹 + Spring Boot API]
            Proxy -->|활성 색상| Blue
            Proxy -.->|대기 또는 배포 대상| Green
        end

        subgraph DB[MySQL EC2 · t3.small]
            MySQL[(MySQL · Docker 볼륨)]
        end

        subgraph Cache[Redis EC2 · t3.micro]
            Redis[(Redis · 메모리 캐시)]
        end

        subgraph Monitoring[모니터링 EC2 · t3.small]
            Prometheus[Prometheus]
            Grafana[Grafana]
            Grafana --> Prometheus
        end
    end

    Blue --> MySQL
    Blue --> Redis
    Green --> MySQL
    Green --> Redis
    Prometheus -.->|메트릭 수집| App
    Prometheus -.->|메트릭 수집| DB
    Prometheus -.->|메트릭 수집| Cache
```

그림은 Blue가 활성화된 경우를 예시로 든 것이다. 다음 배포에서는 Green이 활성화될 수 있다. 두 색상은 배포 중 잠시 함께 실행되며, Nginx는 검사를 통과한 한쪽으로만 사용자 요청을 보낸다. MySQL·Redis·모니터링은 외부에 공개하지 않으며, Grafana는 SSM 포트 포워딩으로 접속한다.

## 배포 흐름

```mermaid
flowchart LR
    Push[develop 푸시] --> Test[GitHub Actions<br/>백엔드·프론트·Compose 테스트]
    Test --> Build[API·웹 이미지 빌드]
    Build --> Hub[Docker Hub<br/>커밋 SHA 이미지]
    Build --> Deploy[배포 작업]
    Deploy --> Start[EC2 4대 기동]
    Start --> SSM[SSM Run Command]
    SSM --> Source[EC2에서 GitHub 소스 아카이브 다운로드]
    Source --> Services[MySQL → Redis → 모니터링 시작]
    Services --> Candidate[앱 EC2에서 비활성 색상 기동]
    Hub --> Candidate
    Candidate --> Check[상태·HTTP 검사]
    Check --> Switch[Nginx 전환 및 이전 색상 중지]
```

배포가 실패하면 앱 EC2는 기존 활성 색상을 유지하거나 되돌린다. 네 EC2는 부팅 약 4시간 뒤 자동 중지된다. MySQL 데이터는 EC2 중지·재시작 후에도 루트 EBS의 Docker 볼륨에 남고, Redis 캐시는 재시작 시 사라진다.

자세한 운영 절차는 [AWS 배포 운영](aws-deployment.md)을 참고한다.
