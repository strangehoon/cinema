import { useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { api } from './api';
import type { Profile } from './api';

export function AuthPage({ onAuthenticated }: { onAuthenticated: (profile: Profile) => void }) {
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
