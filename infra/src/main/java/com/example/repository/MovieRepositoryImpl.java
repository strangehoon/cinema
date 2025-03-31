package com.example.repository;

import com.example.entity.Movie;
import com.example.enums.Genre;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import static com.example.entity.QMovie.movie;
import static com.example.entity.QScreening.screening;
import static com.example.entity.QTheater.theater;
import static com.querydsl.core.types.dsl.Expressions.booleanTemplate;

@Repository
@RequiredArgsConstructor
public class MovieRepositoryImpl implements MovieRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<Movie> searchMoviesWithScreenings(String title, Genre genre) {
        return jpaQueryFactory
                .selectFrom(movie)
                .leftJoin(movie.screenings, screening).fetchJoin()
                .leftJoin(screening.theater, theater).fetchJoin()
                .where(
                        filterByTitleFTS(title),
                        filterByGenre(genre),
                        movie.releasedAt.before(LocalDateTime.now())
                )
                .fetch();
    }

    private BooleanExpression filterByTitleFTS(String title) {
        return (title != null && !title.isEmpty()) ?
                booleanTemplate("function('match_against', {0}, {1}) > 0", movie.title, title) :
                null;
    }

    private BooleanExpression filterByGenre(Genre genre) {
        return genre != null ? movie.genre.eq(genre) : null;
    }
}
