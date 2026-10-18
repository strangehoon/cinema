import { useEffect, useState } from 'react';
import { Link, Route, Routes, useLocation, useNavigate, useParams, useSearchParams } from 'react-router-dom';
import { loadTossPayments } from '@tosspayments/tosspayments-sdk';
import { api, canSelect, formatDate, formatMoney, seatLabel } from './api';
import type { CreatedOrder, Movie, MoviePage, Order, Profile, Screening, Seat, SeatMap } from './api';

const genres = ['전체', 'DRAMA', 'ACTION', 'ROMANCE', 'COMEDY', 'SF', 'THRILLER', 'HORROR', 'FANTASY', 'MYSTERY'];
const genreNames: Record<string, string> = { DRAMA: '드라마', ACTION: '액션', ROMANCE: '로맨스', COMEDY: '코미디', SF: 'SF', THRILLER: '스릴러', HORROR: '공포', FANTASY: '판타지', MYSTERY: '미스터리' };
const posterFor = (movie: Movie) => movie.poster || `/posters/${String((movie.id - 1) % 12 + 1).padStart(2, '0')}.svg`;
const statusNames: Record<string, string> = {
  READY: '결제 대기', IN_PROGRESS: '결제 확인 중', DONE: '예매 완료',
  REFUNDING: '환불 확인 중', CANCELED: '취소 완료', EXPIRED: '만료됨',
};
const movieIntros: Record<string, string> = {
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

function useLoad<T>(path: string | null, refreshMs?: number) {
  const [value, setValue] = useState<T | null>(null);
  const [error, setError] = useState('');
  useEffect(() => {
    if (!path) return;
    let alive = true;
    const load = () => api<T>(path).then(data => { if (alive) { setValue(data); setError(''); } })
      .catch(e => { if (alive) setError(e.message); });
    void load();
    const timer = refreshMs ? window.setInterval(load, refreshMs) : undefined;
    return () => { alive = false; if (timer) window.clearInterval(timer); };
  }, [path, refreshMs]);
  return { value, error };
}

function App() {
  const [profile, setProfile] = useState<Profile | null>(null);
  const [authReady, setAuthReady] = useState(false);
  const navigate = useNavigate();
  useEffect(() => { api<Profile>('/auth/me').then(setProfile).catch(() => undefined).finally(() => setAuthReady(true)); }, []);
  async function logout() {
    await api('/auth/logout', { method: 'POST' });
    setProfile(null);
    navigate('/');
  }
  return <div className="site-shell">
    <header className="site-header"><div className="container nav-inner">
      <Link className="wordmark" to="/">CINEMA<i /></Link>
      <nav aria-label="주 메뉴"><Link to="/">영화</Link><Link to="/my-bookings">내 예매</Link></nav>
      <div className="nav-account">{authReady && (profile ? <><span>{profile.name}님</span><button className="text-button" onClick={logout}>로그아웃</button></> : <Link className="outline-button" to="/login">로그인</Link>)}</div>
    </div></header>
    <main><Routes>
      <Route path="/" element={<MoviesPage />} />
      <Route path="/movies/:id" element={<MoviePageView />} />
      <Route path="/screenings/:id/seats" element={<SeatsPage profile={profile} />} />
      <Route path="/checkout/:orderId" element={<CheckoutPage />} />
      <Route path="/receipt" element={<ReceiptPage />} />
      <Route path="/toss/success" element={<TossSuccessPage />} />
      <Route path="/toss/fail" element={<TossFailPage />} />
      <Route path="/my-bookings" element={<MyBookingsPage profile={profile} />} />
      <Route path="/login" element={<AuthPage onAuthenticated={setProfile} />} />
      <Route path="*" element={<div className="container empty"><h1>페이지를 찾을 수 없습니다.</h1><Link to="/">영화 보기</Link></div>} />
    </Routes></main>
    <footer className="site-footer"><div className="container"><strong>CINEMA</strong><span>영화의 순간을 예약하다 · 로컬 데모 서비스</span></div></footer>
  </div>;
}

function MoviesPage() {
  const [query, setQuery] = useState('');
  const [search, setSearch] = useState('');
  const [genre, setGenre] = useState('전체');
  const [page, setPage] = useState(0);
  const params = new URLSearchParams({ page: String(page), size: '12' });
  if (search) params.set('title', search);
  if (genre !== '전체') params.set('genre', genre);
  const { value, error } = useLoad<MoviePage>(`/movies?${params}`);
  return <>
    <section className="hero"><div className="container hero-inner">
      <div className="hero-copy"><div className="eyebrow"><span className="eyebrow-line" /> NOW SHOWING</div>
        <h1>오늘의 영화,<br /><em>당신의 자리.</em></h1>
        <p>마음에 남을 장면을 만나보세요.<br />상영 시간부터 좌석까지, 편안하게 예약하세요.</p>
        <a href="#movies" className="primary-button">상영 영화 보기 <span>↗</span></a>
      </div><div className="hero-art"><div className="orb orb-one" /><div className="orb orb-two" /><div className="film-frame"><span>CINEMA</span><small>THE CINEMA EXPERIENCE</small></div><div className="hero-caption">01 / A MOMENT TO REMEMBER</div></div>
    </div></section>
    <section className="catalog container" id="movies"><div className="section-heading"><div><div className="eyebrow">THE COLLECTION</div><h2>지금 상영 중</h2></div><p>보고 싶은 영화를 골라보세요</p></div>
      <div className="filters"><form onSubmit={e => { e.preventDefault(); setPage(0); setSearch(query.trim()); }}><input aria-label="영화 제목 검색" placeholder="영화 제목을 검색해 보세요" value={query} onChange={e => setQuery(e.target.value)} /><button type="submit">검색</button></form>
        <div className="genre-tabs" aria-label="장르 필터">{genres.map(g => <button key={g} className={genre === g ? 'active' : ''} onClick={() => { setGenre(g); setPage(0); }}>{g === '전체' ? g : genreNames[g]}</button>)}</div>
      </div>
      {error && <div className="notice error">{error}</div>}
      {!value && !error && <div className="loading">영화를 불러오는 중입니다…</div>}
      {value?.content.length === 0 && <div className="empty">조건에 맞는 영화가 없습니다.</div>}
      <div className="movie-grid">{value?.content.map((movie, index) => <Link key={movie.id} to={`/movies/${movie.id}`} className="movie-card">
        <div className={`poster poster-${index % 6}`}><img src={posterFor(movie)} alt="" /><span className="poster-num">{String(movie.id).padStart(2, '0')}</span><span className="poster-title">{movie.title}</span><span className="poster-caption">CINEMA PRESENTS</span></div>
        <div className="movie-meta"><span className="rating">{movie.rating.replace('R_', '')}</span><h3>{movie.title}</h3><p>{genreNames[movie.genre] || movie.genre} · {movie.runningTime}분</p><span className="card-link">상영 시간 보기 ↗</span></div>
      </Link>)}</div>
      {value && value.totalElements > value.size && <div className="pagination"><button disabled={page === 0} onClick={() => setPage(page - 1)}>이전</button><span>{page + 1} / {Math.ceil(value.totalElements / value.size)}</span><button disabled={(page + 1) * value.size >= value.totalElements} onClick={() => setPage(page + 1)}>다음</button></div>}
    </section>
  </>;
}

function MoviePageView() {
  const { id } = useParams();
  const { value: movie, error } = useLoad<Movie>(`/movies/${id}`);
  if (error) return <div className="container empty">{error}</div>;
  if (!movie) return <div className="container loading">영화를 불러오는 중입니다…</div>;
  const dates = [...movie.screenings].sort((a, b) => a.startsAt.localeCompare(b.startsAt))
    .reduce<Record<string, Screening[]>>((groups, screening) => {
      const date = screening.startsAt.slice(0, 10);
      (groups[date] ||= []).push(screening);
      return groups;
    }, {});
  return <div className="container detail-page"><Link className="back-link" to="/">← 영화 목록</Link>
    <div className="detail-layout"><div className="detail-poster poster poster-1"><img src={posterFor(movie)} alt="" /><span className="poster-title">{movie.title}</span><span className="poster-caption">CINEMA PRESENTS</span></div>
      <div className="detail-content"><div className="eyebrow">NOW SHOWING</div><h1>{movie.title}</h1><p className="detail-subtitle">{genreNames[movie.genre] || movie.genre} <span>·</span> {movie.runningTime}분 <span>·</span> {movie.rating.replace('R_', '')}세 관람가</p>
        <p className="movie-introduction" data-testid="movie-introduction">{movieIntros[movie.title] || '새로운 이야기가 스크린 위에서 펼쳐집니다.'}</p>
        <div className="divider" /><h2>상영 시간</h2><p className="muted">원하는 회차를 선택하고 좌석을 예약하세요.</p>
        {movie.screenings.length === 0 && <div className="notice">예매 가능한 상영 회차가 없습니다.</div>}
        <div className="screening-list">{Object.entries(dates).map(([date, sessions]) => <section key={date} className="screening-date-group"><h3 data-testid="screening-date">{new Intl.DateTimeFormat('ko-KR', { month: 'long', day: 'numeric', weekday: 'long' }).format(new Date(`${date}T12:00:00`))}</h3>{sessions?.map(s => <Link key={s.id} to={`/screenings/${s.id}/seats`} className="screening-item" role="button" aria-label={`${s.theater} ${formatDate(s.startsAt)}`}>
          <div><strong>{formatDate(s.startsAt)}</strong><span>{s.theater} · {s.availableSeats}/{s.totalSeats}석 남음</span></div><span className="screening-arrow">{s.availableSeats ? '좌석 선택 ↗' : '매진'}</span>
        </Link>)}</section>)}</div>
      </div></div></div>;
}

function SeatsPage({ profile }: { profile: Profile | null }) {
  const { id } = useParams();
  const navigate = useNavigate();
  const { value: map, error: loadError } = useLoad<SeatMap>(`/screenings/${id}/seats`, 5000);
  const [selected, setSelected] = useState<Seat[]>([]);
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);
  function toggle(seat: Seat) {
    if (seat.status !== 'AVAILABLE') return;
    if (selected.some(s => s.id === seat.id)) { setSelected(selected.filter(s => s.id !== seat.id)); setError(''); return; }
    if (!canSelect(selected, seat)) { setError('같은 줄의 연속된 좌석을 최대 3석까지 선택해 주세요.'); return; }
    setSelected([...selected, seat]); setError('');
  }
  async function book() {
    if (!profile) { navigate('/login', { state: { from: `/screenings/${id}/seats` } }); return; }
    setBusy(true); setError('');
    try {
      const order = await api<CreatedOrder>('/bookings', { method: 'POST', body: JSON.stringify({ screeningId: Number(id), seatIds: selected.map(s => s.id) }) });
      navigate(`/checkout/${order.orderId}`, { state: { order } });
    } catch (e) { setError((e as Error).message); setSelected([]); }
    finally { setBusy(false); }
  }
  if (loadError) return <div className="container empty">{loadError}</div>;
  if (!map) return <div className="container loading">좌석을 불러오는 중입니다…</div>;
  const rows = [...new Set(map.seats.map(s => s.row))].sort((a, b) => a - b);
  return <div className="container seats-page"><Link className="back-link" to={`/movies/${map.screening.movieId}`}>← 상영 시간</Link>
    <div className="step-label">STEP 02 / SEAT SELECTION</div><h1>좌석을 선택해 주세요</h1><p className="muted">{map.screening.movieTitle} · {map.screening.theater} · {formatDate(map.screening.startsAt)}</p>
    <div className="seat-layout"><div className="seat-panel"><div className="screen"><span>SCREEN</span></div><div className="seat-map">{rows.map(row => <div className="seat-row" key={row}><span className="row-label">{String.fromCharCode(64 + row)}</span>{map.seats.filter(s => s.row === row).sort((a,b) => a.col - b.col).map(seat => <button key={seat.id} aria-label={seatLabel(seat)} title={seatLabel(seat)} className={`seat ${seat.status !== 'AVAILABLE' ? 'occupied' : selected.some(s => s.id === seat.id) ? 'selected' : ''}`} disabled={seat.status !== 'AVAILABLE'} onClick={() => toggle(seat)}>{seat.col}</button>)}</div>)}</div>
        <div className="seat-legend"><span><i className="legend-available" />선택 가능</span><span><i className="legend-selected" />선택 좌석</span><span><i className="legend-occupied" />예약 완료</span></div>
      </div><aside className="booking-summary"><div className="eyebrow">YOUR SELECTION</div><h2>예매 정보</h2><div className="summary-row"><span>남은 좌석</span><strong>{map.availableSeats} / {map.totalSeats}</strong></div><div className="summary-row"><span>선택 좌석</span><strong>{selected.length ? selected.map(seatLabel).join(', ') : '선택 전'}</strong></div><div className="summary-row"><span>매수</span><strong>{selected.length}매</strong></div><div className="summary-total"><span>결제 금액</span><strong>{formatMoney(selected.reduce((sum, s) => sum + s.price, 0))}</strong></div>
        <button className="primary-button full" disabled={!selected.length || busy} onClick={book}>{busy ? '처리 중…' : '예매하기 →'}</button><p className="fine-print">같은 줄의 연속된 좌석을 최대 3석까지 선택할 수 있습니다.</p>
      </aside></div>{error && <div className="notice error">{error}</div>}</div>;
}

function CheckoutPage() {
  const { orderId } = useParams();
  const navigate = useNavigate();
  const location = useLocation();
  const created = (location.state as { order?: CreatedOrder } | null)?.order;
  const { value: fetched } = useLoad<Order>(created ? null : `/bookings/${orderId}`);
  const { value: paymentConfig } = useLoad<{ mode: string; clientKey: string }>('/bookings/payment-config');
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const [now, setNow] = useState(Date.now());
  useEffect(() => {
    const timer = window.setInterval(() => setNow(Date.now()), 1000);
    return () => window.clearInterval(timer);
  }, []);
  const expiresAt = created?.expiresAt || fetched?.expiresAt;
  const secondsLeft = expiresAt ? Math.max(0, Math.ceil((new Date(expiresAt).getTime() - now) / 1000)) : 0;
  const unavailable = Boolean((fetched && fetched.status !== 'READY') || (expiresAt && secondsLeft === 0));
  async function pay() {
    setBusy(true); setError('');
    try {
      if (paymentConfig?.mode === 'toss') {
        if (!paymentConfig.clientKey || !orderId) throw new Error('토스 테스트 키를 설정해 주세요.');
        const sdk = await loadTossPayments(paymentConfig.clientKey);
        const payment = sdk.payment({ customerKey: `cinema-${orderId}` });
        await payment.requestPayment({
          method: 'CARD', amount: { currency: 'KRW', value: created?.totalAmount || fetched?.amount || 0 },
          orderId, orderName: created?.orderName || fetched?.name || '영화 예매',
          successUrl: `${window.location.origin}/toss/success`,
          failUrl: `${window.location.origin}/toss/fail`,
        });
      } else {
        await api(`/bookings/${orderId}/mock-payment`, { method: 'POST' });
        navigate('/receipt', { state: { orderId } });
      }
    }
    catch (e) { setError((e as Error).message); }
    finally { setBusy(false); }
  }
  async function tryFailedMockPayment() {
    setBusy(true); setError('');
    try { await api(`/bookings/${orderId}/mock-payment/fail`, { method: 'POST' }); }
    catch (e) { setError((e as Error).message); }
    finally { setBusy(false); }
  }
  return <div className="container narrow-page"><div className="step-label">STEP 03 / CHECKOUT</div><h1>결제하기</h1><p className="muted">좌석은 최대 5분 동안 보관됩니다. 결제를 완료해 주세요.</p>
    <div className="checkout-card"><div className="eyebrow">ORDER SUMMARY</div><h2>{created?.orderName || fetched?.name || '주문을 불러오는 중입니다…'}</h2><div className="summary-row"><span>주문 번호</span><strong>{orderId}</strong></div><div className="summary-row"><span>결제 수단</span><strong>{paymentConfig?.mode === 'toss' ? '토스 테스트 카드' : '모의 결제'}</strong></div><div className="summary-row"><span>좌석 보유 남은 시간</span><strong data-testid="hold-countdown">{Math.floor(secondsLeft / 60)}:{String(secondsLeft % 60).padStart(2, '0')}</strong></div><div className="summary-total"><span>결제 금액</span><strong>{formatMoney(created?.totalAmount || fetched?.amount || 0)}</strong></div>
      <button className="primary-button full" onClick={pay} disabled={busy || !expiresAt || unavailable || !paymentConfig}>{busy ? '결제 중…' : paymentConfig?.mode === 'toss' ? '토스 테스트 카드로 결제 →' : '모의 결제 완료하기 →'}</button>
      {paymentConfig?.mode !== 'toss' && <button className="text-button mock-fail-button" onClick={tryFailedMockPayment} disabled={busy || !expiresAt || unavailable}>결제 실패 체험</button>}</div>
    {unavailable && <div className="notice error">결제 대기 시간이 끝났거나 이미 처리된 주문입니다. 예매 내역을 확인해 주세요.</div>}
    {error && <div className="notice error">{error}</div>}</div>;
}

function TossSuccessPage() {
  const [params] = useSearchParams();
  const navigate = useNavigate();
  const [error, setError] = useState('');
  useEffect(() => {
    const orderId = params.get('orderId');
    const paymentKey = params.get('paymentKey');
    const amount = Number(params.get('amount'));
    if (!orderId || !paymentKey || !Number.isSafeInteger(amount) || amount <= 0) {
      setError('결제 승인 정보가 올바르지 않습니다.'); return;
    }
    api(`/bookings/${orderId}/toss-confirm`, {
      method: 'POST', body: JSON.stringify({ paymentKey, amount }),
    }).then(() => navigate('/receipt', { replace: true, state: { orderId } }))
      .catch(e => setError((e as Error).message));
  }, [params, navigate]);
  return <div className="container result-page">{error ? <><h1>결제를 확인하지 못했습니다.</h1><div className="notice error">{error}</div><Link to="/my-bookings">예매 내역 확인</Link></> : <div className="loading">결제 승인 결과를 확인하고 있습니다…</div>}</div>;
}

function TossFailPage() {
  const [params] = useSearchParams();
  return <div className="container result-page"><h1>결제를 완료하지 못했습니다.</h1>
    <div className="notice error">{params.get('message') || '결제 요청이 취소되었습니다.'}</div>
    <Link className="primary-button" to={params.get('orderId') ? `/checkout/${params.get('orderId')}` : '/my-bookings'}>다시 확인하기 →</Link>
  </div>;
}

function ReceiptPage() {
  return <div className="container result-page"><div className="result-icon">✓</div><div className="eyebrow">BOOKING CONFIRMED</div><h1>결제가 완료되었습니다</h1><p>영화관에서 즐거운 시간 보내세요.</p><Link className="primary-button" to="/my-bookings">내 예매 확인하기 →</Link></div>;
}

function MyBookingsPage({ profile }: { profile: Profile | null }) {
  const { value: orders, error: loadError } = useLoad<Order[]>(profile ? '/bookings' : null);
  const [updated, setUpdated] = useState<Record<string, Order>>({});
  const [error, setError] = useState('');
  async function cancel(orderId: string) {
    if (!window.confirm('예매를 취소하시겠습니까?')) return;
    try { const order = await api<Order>(`/bookings/${orderId}/cancel`, { method: 'POST' }); setUpdated({ ...updated, [orderId]: order }); }
    catch (e) { setError((e as Error).message); }
  }
  return <div className="container bookings-page"><div className="eyebrow">MY CINEMA</div><h1>내 예매</h1><p className="muted">예매 내역과 결제 상태를 확인하세요.</p>
    {!profile && <div className="notice">예매 내역을 보려면 <Link to="/login">로그인</Link>해 주세요.</div>}
    {loadError && <div className="notice error">{loadError}</div>}{error && <div className="notice error">{error}</div>}
    {profile && !orders && !loadError && <div className="loading">예매 내역을 불러오는 중입니다…</div>}
    {orders?.length === 0 && <div className="empty">아직 예매 내역이 없습니다. <Link to="/">영화 보러 가기 →</Link></div>}
    <div className="order-list">{orders?.map(original => { const order = updated[original.orderId] || original; return <article className="order-card" key={order.orderId}>
      <div className="order-card-top"><span className="eyebrow">ORDER {order.orderId.slice(0, 8)}</span><span className={`status status-${order.status.toLowerCase()}`}>{statusNames[order.status] || order.status}</span></div>
      <h2>{order.name}</h2><p>{order.startsAt ? formatDate(order.startsAt) : ''} · {order.seats.join(', ')}</p><div className="order-bottom"><strong>{formatMoney(order.amount)}</strong><div className="order-actions">{order.status === 'READY' && <Link className="outline-button" to={`/checkout/${order.orderId}`}>결제 계속</Link>}{(order.status === 'DONE' || order.status === 'READY') && <button className="outline-button" onClick={() => cancel(order.orderId)}>예매 취소</button>}</div></div>
    </article>; })}</div></div>;
}

function AuthPage({ onAuthenticated }: { onAuthenticated: (profile: Profile) => void }) {
  const navigate = useNavigate();
  const location = useLocation();
  const [signup, setSignup] = useState(false);
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [name, setName] = useState('');
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);
  async function submit(event: React.FormEvent) {
    event.preventDefault(); setBusy(true); setError('');
    try {
      const profile = await api<Profile>(signup ? '/auth/signup' : '/auth/login', {
        method: 'POST', body: JSON.stringify({ email, password, ...(signup ? { name } : {}) }),
      });
      onAuthenticated(profile);
      navigate((location.state as { from?: string } | null)?.from || '/');
    } catch (e) { setError((e as Error).message); }
    finally { setBusy(false); }
  }
  return <div className="container auth-page"><div className="auth-panel"><div className="eyebrow">WELCOME TO CINEMA</div><h1>{signup ? '회원가입' : '다시 만나 반가워요'}</h1><p className="muted">영화의 순간이 여기서 시작됩니다.</p><form onSubmit={submit}>
    {signup && <label>이름<input required value={name} onChange={e => setName(e.target.value)} placeholder="이름" /></label>}
    <label>이메일<input required type="email" value={email} onChange={e => setEmail(e.target.value)} placeholder="you@example.com" /></label>
    <label>비밀번호<input required type="password" minLength={8} value={password} onChange={e => setPassword(e.target.value)} placeholder="8자 이상" /></label>
    {error && <div className="notice error">{error}</div>}
    <button className="primary-button full" disabled={busy}>{busy ? '처리 중…' : signup ? '계정 만들기' : '로그인'}</button></form>
    <button className="text-button auth-switch" onClick={() => { setSignup(!signup); setError(''); }}>{signup ? '이미 계정이 있나요? 로그인' : '처음이신가요? 회원가입'}</button>
    <p className="fine-print">체험 계정: demo@cinema.local / cinema1234</p></div></div>;
}

export default App;
