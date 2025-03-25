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
                        filterByTitle(title),
                        filterByGenre(genre),
                        movie.releaseDate.before(LocalDateTime.now())
                )
                .fetch();
    }

    private BooleanExpression filterByTitle(String title) {
        return title != null ? movie.title.contains(title) : null;
    }

    private BooleanExpression filterByGenre(Genre genre) {
        return genre != null ? movie.genre.eq(genre) : null;
    }
}
