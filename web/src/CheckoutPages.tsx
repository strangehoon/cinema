import { useEffect, useState } from 'react';
import { Link, useLocation, useNavigate, useParams, useSearchParams } from 'react-router-dom';
import { loadTossPayments } from '@tosspayments/tosspayments-sdk';
import { api, formatMoney } from './api';
import type { CreatedOrder, Order } from './api';
import { useLoad } from './useLoad';

export function CheckoutPage() {
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

export function TossSuccessPage() {
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

export function TossFailPage() {
  const [params] = useSearchParams();
  return <div className="container result-page"><h1>결제를 완료하지 못했습니다.</h1>
    <div className="notice error">{params.get('message') || '결제 요청이 취소되었습니다.'}</div>
    <Link className="primary-button" to={params.get('orderId') ? `/checkout/${params.get('orderId')}` : '/my-bookings'}>다시 확인하기 →</Link>
  </div>;
}

export function ReceiptPage() {
  return <div className="container result-page"><div className="result-icon">✓</div><div className="eyebrow">BOOKING CONFIRMED</div><h1>결제가 완료되었습니다</h1><p>영화관에서 즐거운 시간 보내세요.</p><Link className="primary-button" to="/my-bookings">내 예매 확인하기 →</Link></div>;
}
