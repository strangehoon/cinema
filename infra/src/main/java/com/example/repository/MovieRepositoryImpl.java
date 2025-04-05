package com.example.repository;

import com.example.entity.Movie;
import com.example.enums.Genre;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import static com.example.entity.QMovie.movie;
import static com.example.entity.QScreening.screening;

@Repository
@RequiredArgsConstructor
public class MovieRepositoryImpl implements MovieRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<Movie> searchMoviesWithScreenings(String title, Genre genre, Long theaterId, Pageable pageable) {

        // Step 1: 특정 극장에서 상영 중인 movieId 조회
        List<Long> screeningMovieIds = jpaQueryFactory
                .select(screening.movie.id)
                .from(screening)
                .where(screening.theater.id.eq(theaterId))
                .fetch();

        if (screeningMovieIds.isEmpty()) {
            return Page.empty(); // 상영 중인 영화 없음
        }

        // 공통 where 조건
        BooleanExpression commonCondition = movie.id.in(screeningMovieIds)
                .and(filterByTitleStartsWith(title))
                .and(filterByGenre(genre))
                .and(movie.releasedDate.before(LocalDate.now()));

        // Step 2-1: 본문 조회 (페이징 적용)
        List<Movie> content = jpaQueryFactory
                .selectFrom(movie)
                .where(commonCondition)
                .orderBy(movie.releasedDate.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // Step 2-2: 전체 개수 조회 (페이징 정보용)
        long total = jpaQueryFactory
                .select(movie.count())
                .from(movie)
                .where(commonCondition)
                .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }

    private BooleanExpression filterByTitleStartsWith(String title) {
        return (title != null && !title.isEmpty()) ?
                movie.title.like(title + "%") :
                null;
    }

    private BooleanExpression filterByGenre(Genre genre) {
        return genre != null ? movie.genre.eq(genre) : null;
    }
}
