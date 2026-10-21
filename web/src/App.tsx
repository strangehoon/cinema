import { useEffect, useState } from 'react';
import { Link, Route, Routes, useNavigate } from 'react-router-dom';
import { api } from './api';
import type { Profile } from './api';
import { MoviesPage, MoviePageView } from './CatalogPages';
import { SeatsPage } from './SeatsPage';
import { CheckoutPage, ReceiptPage, TossSuccessPage, TossFailPage } from './CheckoutPages';
import { MyBookingsPage } from './MyBookingsPage';
import { AuthPage } from './AuthPage';

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

export default App;
