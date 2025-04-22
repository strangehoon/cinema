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
import java.util.List;
import static com.example.entity.QMovie.movie;
import static com.example.entity.QScreening.screening;
import static com.example.entity.QTheater.theater;

@Repository
@RequiredArgsConstructor
public class MovieRepositoryImpl implements MovieRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

//    @Override
//    public Page<Movie> searchMoviesWithScreenings(String title, Genre genre, Pageable pageable) {
//
//        List<Movie> movies = jpaQueryFactory
//                .selectFrom(movie)
//                .where(
//                        filterByTitle(title),
//                        filterByGenre(genre)
//                )
//                .orderBy(movie.releasedDate.desc())
//                .offset(pageable.getOffset())
//                .limit(pageable.getPageSize())
//                .fetch();
//
//        // 페이징을 위한 전체 개수 조회
//        long total = jpaQueryFactory
//                .select(movie.count())
//                .from(movie)
//                .where(
//                        filterByTitle(title),
//                        filterByGenre(genre)
//                )
//                .fetchOne();
//        return new PageImpl<>(movies, pageable, total);
//    }
    @Override
    public Page<Movie> searchMoviesWithScreenings(String title, Genre genre, Pageable pageable) {

        // 1단계: Movie ID만 조회 ()
        List<Long> movieIds = jpaQueryFactory
                .select(movie.id)
                .from(movie)
                .where(
                        filterByTitle(title),
                        filterByGenre(genre)
                )
                .orderBy(movie.releasedDate.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        if (movieIds.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        // 2단계: Movie + Screening + Theater fetch join으로 조회
        List<Movie> movies = jpaQueryFactory
                .selectFrom(movie)
                .leftJoin(movie.screenings, screening).fetchJoin()
                .leftJoin(screening.theater, theater).fetchJoin()
                .where(movie.id.in(movieIds))
                .distinct()
                .fetch();

        // 페이징을 위한 전체 개수 조회
        long total = jpaQueryFactory
                .select(movie.count())
                .from(movie)
                .where(
                        filterByTitle(title),
                        filterByGenre(genre)
                )
                .fetchOne();
        return new PageImpl<>(movies, pageable, total);
    }


    private BooleanExpression filterByTitle(String title) {
        return (title != null && !title.isBlank()) ? movie.title.startsWith(title) : null;
    }

    private BooleanExpression filterByGenre(Genre genre) {
        return genre != null ? movie.genre.eq(genre) : null;
    }
}
