import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { api, canSelect, formatDate, formatMoney, seatLabel } from './api';
import type { CreatedOrder, Profile, Seat, SeatMap } from './api';
import { useLoad } from './useLoad';

export function SeatsPage({ profile }: { profile: Profile | null }) {
  const { id } = useParams();
  const navigate = useNavigate();
  const { value: map, error: loadError, reload } = useLoad<SeatMap>(`/screenings/${id}/seats`, 5000);
  const [selected, setSelected] = useState<Seat[]>([]);
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);
  useEffect(() => {
    if (!map) { setSelected([]); return; }
    const available = new Map(map.seats.filter(seat => seat.status === 'AVAILABLE')
      .map(seat => [seat.id, seat]));
    setSelected(previous => previous.map(seat => available.get(seat.id))
      .filter((seat): seat is Seat => seat !== undefined));
  }, [map]);
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
    } catch (e) { setError((e as Error).message); setSelected([]); reload(); }
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
