package com.example.db.repository;

import com.example.db.entity.Movie;
import com.example.db.enums.Genre;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import java.util.List;
import static com.example.db.entity.QMovie.movie;
import static com.example.db.entity.QScreening.screening;
import static com.example.db.entity.QTheater.theater;

@Repository
@RequiredArgsConstructor
public class MovieRepositoryImpl implements MovieRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<Movie> searchMoviesWithScreenings(String title, Genre genre, Pageable pageable) {

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

        List<Movie> movies = jpaQueryFactory
                .selectFrom(movie)
                .leftJoin(movie.screenings, screening).fetchJoin()
                .leftJoin(screening.theater, theater).fetchJoin()
                .where(movie.id.in(movieIds))
                .distinct()
                .fetch();

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