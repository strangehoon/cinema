import { useState } from 'react';
import { Link } from 'react-router-dom';
import { api, formatDate, formatMoney } from './api';
import type { Order, Profile } from './api';
import { statusNames } from './display';
import { useLoad } from './useLoad';

export function MyBookingsPage({ profile }: { profile: Profile | null }) {
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
