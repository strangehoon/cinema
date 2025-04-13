import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
  vus: 30,            // 100명의 동시 사용자
  duration: '30m',     // 짧은 시간에 집중 부하
};

export default function () {
  const url = 'http://host.docker.internal:8080/reservations';

  const userId = __VU; // __VU는 각 가상 사용자에게 고유한 ID를 부여함 (1~100)

  const payload = JSON.stringify({
    memberId: userId,
    screeningId: 1,
    seatIds: [1, 2, 3], // 모든 유저가 동일한 좌석을 요청함
  });

  const params = {
    headers: {
      'Content-Type': 'application/json',
    },
  };

  const res = http.post(url, payload, params);

  check(res, {
    'status is 200 or 409': (r) => r.status === 200 || r.status === 409,
    'reservation success or conflict': (r) =>
      r.status === 200 || r.status === 409,
  });

  sleep(1); // 사용자별 Think Time
}
