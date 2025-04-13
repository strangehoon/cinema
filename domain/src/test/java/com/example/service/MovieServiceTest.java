package com.example.service;

import com.example.config.IntegrationServiceTest;
import com.example.dto.response.MovieScreeningServiceResponse;
import com.example.dto.response.PageResponse;
import com.example.dto.response.ScreeningServiceResponse;
import com.example.entity.Movie;
import com.example.entity.Screening;
import com.example.entity.Theater;
import com.example.enums.Genre;
import com.example.enums.Rating;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import static com.example.enums.Genre.DRAMA;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

@Transactional
class MovieServiceTest extends IntegrationServiceTest {

    @Nested
    @DisplayName("상영중인 영화 조회")
    class getMoviesWithScreenings_success {

        @Test
        @DisplayName("영화 제목과 장르로 조회하면 해당 영화와 관련된 상영 정보가 반환된다")
        void getMoviesWithScreenings() {
            // given
            Movie movie = saveMovie("movie1", DRAMA);
            Theater theater = saveTheater("theater1");
            Screening screening = saveScreening(movie, theater, LocalDate.now().plusDays(1));
            em.clear();

            // when
            PageResponse<MovieScreeningServiceResponse> result =
                    movieService.getMoviesWithScreenings("movie1", "drama", 0, 10);

            // then
            MovieScreeningServiceResponse response = result.getContent().get(0);
            assertThat(response.getTitle()).isEqualTo("movie1");
            assertThat(response.getGenre()).isEqualTo("DRAMA");

            assertThat(response.getScreeningServiceResponses())
                    .hasSize(1)
                    .extracting(
                            ScreeningServiceResponse::getDate,
                            ScreeningServiceResponse::getTheaterName
                    )
                    .containsExactly(
                            tuple(LocalDate.now().plusDays(1), "theater1")
                    );
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

    private Theater saveTheater(String name) {
        return theaterRepository.save(Theater.builder()
                .name(name)
                .build());
    }

    private Screening saveScreening(Movie movie, Theater theater, LocalDate localDate) {
        return screeningRepository.save(Screening.builder()
                .date(localDate)
                .startedAt(LocalDateTime.of(2026, 1, 1, 14, 0))
                .endedAt(LocalDateTime.of(2026, 1, 1, 16, 0))
                .movie(movie)
                .theater(theater)
                .build());
    }
}