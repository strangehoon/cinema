package com.example.repository;

import com.example.config.IntegrationRepositoryTest;
import com.example.db.entity.Movie;
import com.example.db.enums.Genre;
import com.example.db.enums.Rating;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import static com.example.db.enums.Genre.DRAMA;
import static com.example.db.enums.Genre.SF;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@Transactional
class MovieRepositoryCustomTest extends IntegrationRepositoryTest {

    @Nested
    @DisplayName("searchMoviesWithScreenings 메서드")
    class SearchMoviesWithScreenings {

        @Test
        @DisplayName("제목과 장르로 영화를 검색하고 페이징 처리한다")
        void success_with_title_genre() {
            saveMovie("movie1", SF);
            saveMovie("movie2", DRAMA);
            saveMovie("movie3", DRAMA);

            // when
            Page<Movie> result = movieRepositoryImpl.searchMoviesWithScreenings(
                    "movie",                // title startsWith "A"
                    DRAMA,       // genre filter
                    PageRequest.of(0, 10)
            );

            // then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent())
                    .extracting(Movie::getTitle, Movie::getGenre)
                    .containsExactly(
                            tuple("movie2", DRAMA),
                            tuple("movie3", DRAMA)
                    );
            assertThat(result.getTotalElements()).isEqualTo(2);
        }

        @Test
        @DisplayName("제목과 장르가 없을 때 전체 영화가 검색된다")
        void success_nothing() {
            // given
            saveMovie("movie1", SF);
            saveMovie("movie2", DRAMA);
            saveMovie("movie3", DRAMA);

            // when
            Page<Movie> result = movieRepositoryImpl.searchMoviesWithScreenings(
                    null,
                    null,
                    PageRequest.of(0, 10)
            );

            // then
            assertThat(result.getContent()).hasSize(3);
            assertThat(result.getContent())
                    .extracting(Movie::getTitle, Movie::getGenre)
                    .containsExactly(
                            tuple("movie1", SF),
                            tuple("movie2", DRAMA),
                            tuple("movie3", DRAMA)
                    );
            assertThat(result.getTotalElements()).isEqualTo(3);
        }
    }

    private Movie saveMovie(String title, Genre genre) {
        return movieRepository.save(Movie.builder()
                .title(title)
                .rating(Rating.R_12)
                .releasedDate(LocalDate.of(2000, 1, 1))
                .thumbnailImage("movie1-thumbnail.jpg")
                .runningTimeMin(120)
                .genre(genre)
                .build());
    }

    private Movie saveMovie(String title, Genre genre, LocalDate date) {
        return movieRepository.save(Movie.builder()
                .title(title)
                .rating(Rating.R_12)
                .releasedDate(date)
                .thumbnailImage("movie1-thumbnail.jpg")
                .runningTimeMin(120)
                .genre(genre)
                .build());
    }
}