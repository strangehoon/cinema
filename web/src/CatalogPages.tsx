import { useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { formatDate } from './api';
import type { Movie, MoviePage, Screening } from './api';
import { genres, genreNames, movieIntros, posterFor } from './display';
import { useLoad } from './useLoad';

export function MoviesPage() {
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

export function MoviePageView() {
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
