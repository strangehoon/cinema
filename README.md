# Redis_project  

## ERD
![img.png](img.png)

## Multi Module 구조
Layered Architecture  패턴을 적용하여 설계.

**api**
* 클라이언트와의 인터페이스 역할을 담당.
* HTTP 요청을 받아 domain 모듈의 비즈니스 로직을 실행하고, 결과를 응답으로 반환.

**domain**
* 비즈니스 로직의 핵심을 담당.
* API로부터 전달받은 요청을 처리하고, 필요한 데이터는 infra 모듈을 통해 조회하거나 저장.

**infra**
* 데이터베이스, 외부 시스템 연동, 저장소 관련 처리를 담당.

**common**
* 전 모듈에서 공통으로 사용하는 유틸, 설정, 예외처리 등을 담당.

