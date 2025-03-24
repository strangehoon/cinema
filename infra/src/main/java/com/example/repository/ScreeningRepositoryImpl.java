package com.example.repository;

import com.example.entity.Screening;
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
public class ScreeningRepositoryImpl implements ScreeningRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<Screening> searchScreenings(int theaterId, String title, Genre genre) {
        return jpaQueryFactory
                .selectFrom(screening)
                .join(screening.movie, movie).fetchJoin()
                .join(screening.theater, theater).fetchJoin()
                .where(
                        screening.theater.id.eq(theaterId),
                        filterByTitle(title),
                        filterByGenre(genre),
                        movie.releaseDate.before(LocalDateTime.now())
                )
                .fetch();
    }

    private BooleanExpression filterByTitle(String title) {
        return title != null ? movie.title.eq(title) : null;
    }

    private BooleanExpression filterByGenre(Genre genre) {
        return genre != null ? movie.genre.eq(genre) : null;
    }
}
