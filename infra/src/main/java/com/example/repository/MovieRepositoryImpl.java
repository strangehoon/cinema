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

@Repository
@RequiredArgsConstructor
public class MovieRepositoryImpl implements MovieRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<Movie> searchMoviesWithScreenings(String title, Genre genre, Pageable pageable) {

        List<Movie> movies = jpaQueryFactory
                .selectFrom(movie)
                .where(
                        filterByTitle(title),
                        filterByGenre(genre)
                )
                .orderBy(movie.releasedDate.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
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
