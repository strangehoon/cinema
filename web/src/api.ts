export type Profile = { id: number; email: string; name: string };
export type Screening = {
  id: number; movieId: number; movieTitle: string; theater: string;
  startsAt: string; endsAt: string; totalSeats: number; availableSeats: number;
};
export type Movie = {
  id: number; title: string; genre: string; rating: string;
  runningTime: number; poster: string; screenings: Screening[];
};
export type MoviePage = { content: Movie[]; totalElements: number; page: number; size: number };
export type Seat = { id: number; row: number; col: number; price: number; status: string };
export type SeatMap = { screening: Screening; totalSeats: number; availableSeats: number; seats: Seat[] };
export type Order = { orderId: string; name: string; amount: number; status: string; expiresAt?: string; startsAt?: string; seats: string[] };
export type CreatedOrder = { orderId: string; orderName: string; totalAmount: number; expiresAt: string };

let csrfToken: string | null = null;
async function csrf() {
  if (csrfToken) return csrfToken;
  const response = await fetch('/api/auth/csrf', { credentials: 'same-origin' });
  csrfToken = await response.text();
  return csrfToken;
}

export async function api<T>(path: string, init: RequestInit = {}): Promise<T> {
  const method = init.method?.toUpperCase() ?? 'GET';
  const headers = new Headers(init.headers);
  if (init.body) headers.set('Content-Type', 'application/json');
  if (method !== 'GET' && method !== 'HEAD') headers.set('X-XSRF-TOKEN', await csrf());
  const response = await fetch(`/api${path}`, { ...init, headers, credentials: 'same-origin' });
  if (!response.ok) {
    const body = await response.json().catch(() => null);
    throw new Error(body?.message || (response.status === 401 ? '로그인이 필요합니다.' : '요청을 처리하지 못했습니다.'));
  }
  if (response.status === 204) return undefined as T;
  const raw = await response.text();
  return raw ? JSON.parse(raw) as T : undefined as T;
}

export const formatMoney = (amount: number) => `${amount.toLocaleString('ko-KR')}원`;
export const formatDate = (value: string) => new Intl.DateTimeFormat('ko-KR', {
  month: 'long', day: 'numeric', weekday: 'short', hour: '2-digit', minute: '2-digit',
}).format(new Date(value));
export const seatLabel = (seat: Seat) => `${String.fromCharCode(64 + seat.row)}${seat.col}`;

export function canSelect(current: Seat[], next: Seat) {
  if (current.some(s => s.id === next.id)) return true;
  if (current.length >= 3) return false;
  const seats = [...current, next];
  if (seats.some(s => s.row !== seats[0].row)) return false;
  const cols = seats.map(s => s.col).sort((a, b) => a - b);
  return cols.every((col, index) => index === 0 || col === cols[index - 1] + 1);
}
