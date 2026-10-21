import type { Movie } from './api';
export const genres = ['전체', 'DRAMA', 'ACTION', 'ROMANCE', 'COMEDY', 'SF', 'THRILLER', 'HORROR', 'FANTASY', 'MYSTERY'];
export const genreNames: Record<string, string> = { DRAMA: '드라마', ACTION: '액션', ROMANCE: '로맨스', COMEDY: '코미디', SF: 'SF', THRILLER: '스릴러', HORROR: '공포', FANTASY: '판타지', MYSTERY: '미스터리' };
export const posterFor = (movie: Movie) => movie.poster || `/posters/${String((movie.id - 1) % 12 + 1).padStart(2, '0')}.svg`;
export const statusNames: Record<string, string> = {
  READY: '결제 대기', IN_PROGRESS: '결제 확인 중', DONE: '예매 완료',
  REFUNDING: '환불 확인 중', CANCELED: '취소 완료', EXPIRED: '만료됨',
};
export const movieIntros: Record<string, string> = {
  '파도의 기록': '바다에 남겨진 오래된 편지가 서로 다른 시간을 살아온 두 사람을 이어 줍니다.',
  '여름의 끝': '끝나지 않을 것 같던 계절의 마지막 날, 친구들은 오래 미뤄 둔 진심을 마주합니다.',
  '새벽의 도시': '잠들지 않는 도시에서 한 장의 사진이 사라진 사람의 흔적을 드러냅니다.',
  '우리가 남긴 빛': '빛을 잃은 동네에서 작은 극장을 지키려는 이들의 따뜻한 이야기입니다.',
  '열두 번째 밤': '반복되는 밤의 비밀을 풀지 못하면 누구도 아침을 맞을 수 없습니다.',
  '궤도의 밖': '예정된 항로를 벗어난 우주선이 아무도 기록하지 않은 신호를 발견합니다.',
  '오래된 편지': '수십 년 전 발송된 편지가 오늘의 선택을 바꾸기 시작합니다.',
  '비밀의 정원': '닫힌 문 너머의 정원에서 잊고 있던 약속이 다시 자라납니다.',
  '푸른 그림자': '도시를 뒤흔든 사건의 단서는 늘 곁에 있던 그림자 속에 있습니다.',
  '어느 겨울의 노래': '눈 내리는 작은 마을에서 멈췄던 노래가 다시 시작됩니다.',
  '마지막 역': '막차에 오른 낯선 승객들이 저마다 돌아갈 곳을 찾아갑니다.',
  '오늘의 기적': '평범한 하루에 벌어진 작은 우연이 사람들의 삶을 바꿉니다.',
};
