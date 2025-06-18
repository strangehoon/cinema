package com.example.movie;

import com.example.config.IntegrationServiceTest;
import com.example.movie.dto.request.MovieCreateServiceRequest;
import com.example.movie.dto.request.MovieUpdateServiceRequest;
import com.example.movie.dto.response.MovieCreateServiceResponse;
import com.example.movie.dto.response.MovieScreeningServiceResponse;
import com.example.common.dto.PageResponse;
import com.example.movie.dto.response.MovieUpdateServiceResponse;
import com.example.movie.dto.response.ScreeningServiceResponse;
import com.example.db.entity.Movie;
import com.example.db.entity.Screening;
import com.example.db.entity.Theater;
import com.example.db.enums.Genre;
import com.example.db.enums.Rating;
import com.example.movie.exception.MovieException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import static com.example.db.enums.Genre.DRAMA;
import static com.example.movie.exception.MovieErrorCode.MOVIE_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;

@Transactional
class MovieServiceTest extends IntegrationServiceTest {

    @Nested
    @DisplayName("영화 정보 추가")
    class CreateMovie {

        @Test
        @DisplayName("정상적인 요청으로 영화 정보를 추가한다")
        void success() {
            // given
            Theater theater = saveTheater("theater1");

            MovieCreateServiceRequest.ScreeningServiceCreateRequest screeningRequest =
                    MovieCreateServiceRequest.ScreeningServiceCreateRequest.builder()
                            .date(LocalDate.of(2025, 5, 13))
                            .startedAt(LocalDateTime.of(2025, 5, 13, 15, 0))
                            .endedAt(LocalDateTime.of(2025, 5, 13, 17, 30))
                            .theaterId(theater.getId())
                            .build();

            MovieCreateServiceRequest request = MovieCreateServiceRequest.builder()
                    .title("인터스텔라")
                    .rating("R_19")
                    .releasedDate(LocalDate.of(2014, 11, 7))
                    .thumbnailImage("interstellar.jpg")
                    .runningTimeMin(169)
                    .genre("SF")
                    .screenings(List.of(screeningRequest))
                    .build();

            // when
            MovieCreateServiceResponse response = movieService.createMovie(request);

            // then
            assertThat(response.getTitle()).isEqualTo(request.getTitle());
            assertThat(response.getRating()).isEqualTo(request.getRating());
            assertThat(response.getReleasedDate()).isEqualTo(request.getReleasedDate());
            assertThat(response.getThumbnailImage()).isEqualTo(request.getThumbnailImage());
            assertThat(response.getRunningTimeMin()).isEqualTo(request.getRunningTimeMin());
            assertThat(response.getGenre()).isEqualTo(request.getGenre());
            assertThat(response.getScreenings().get(0).getDate()).isEqualTo(screeningRequest.getDate());
            assertThat(response.getScreenings().get(0).getStartedAt()).isEqualTo(screeningRequest.getStartedAt());
            assertThat(response.getScreenings().get(0).getEndedAt()).isEqualTo(screeningRequest.getEndedAt());
            assertThat(response.getScreenings().get(0).getTheaterId()).isEqualTo(screeningRequest.getTheaterId());
        }
    }

    @Nested
    @DisplayName("영화 정보 수정")
    class UpdateMovie {

        @Test
        @DisplayName("정상적인 요청으로 영화 정보를 수정한다")
        void success() {
            // given
            Theater theater = saveTheater("theater1");
            Movie movie = saveMovie("아이언맨", Genre.valueOf("ADVENTURE"));

            MovieUpdateServiceRequest.ScreeningServiceUpdateRequest screeningRequest =
                    MovieUpdateServiceRequest.ScreeningServiceUpdateRequest.builder()
                            .date(LocalDate.of(2025, 5, 13))
                            .startedAt(LocalDateTime.of(2025, 5, 13, 15, 0))
                            .endedAt(LocalDateTime.of(2025, 5, 13, 17, 30))
                            .theaterId(theater.getId())
                            .build();

            MovieUpdateServiceRequest request = MovieUpdateServiceRequest.builder()
                    .title("인터스텔라")
                    .rating("R_19")
                    .releasedDate(LocalDate.of(2014, 11, 7))
                    .thumbnailImage("interstellar.jpg")
                    .runningTimeMin(169)
                    .genre("SF")
                    .screenings(List.of(screeningRequest))
                    .build();

            // when
            MovieUpdateServiceResponse response = movieService.updateMovie(movie.getId(), request);

            // then
            assertThat(response.getTitle()).isEqualTo(request.getTitle());
            assertThat(response.getRating()).isEqualTo(request.getRating());
            assertThat(response.getReleasedDate()).isEqualTo(request.getReleasedDate());
            assertThat(response.getThumbnailImage()).isEqualTo(request.getThumbnailImage());
            assertThat(response.getRunningTimeMin()).isEqualTo(request.getRunningTimeMin());
            assertThat(response.getGenre()).isEqualTo(request.getGenre());
            assertThat(response.getScreenings().get(0).getDate()).isEqualTo(screeningRequest.getDate());
            assertThat(response.getScreenings().get(0).getStartedAt()).isEqualTo(screeningRequest.getStartedAt());
            assertThat(response.getScreenings().get(0).getEndedAt()).isEqualTo(screeningRequest.getEndedAt());
            assertThat(response.getScreenings().get(0).getTheaterId()).isEqualTo(screeningRequest.getTheaterId());
        }
    }

    @Nested
    @DisplayName("영화 정보 삭제")
    class DeleteMovie {

        @Test
        @DisplayName("정상적인 요청으로 영화 정보를 삭제한다")
        void success() {
            // given
            Movie movie = saveMovie("인셉션", Genre.SF);

            // when
            Long deletedMovieId = movieService.deleteMovie(movie.getId());

            // then
            assertThat(deletedMovieId).isEqualTo(movie.getId());
            assertThat(movieRepository.findById(movie.getId())).isEmpty();
        }

        @Test
        @DisplayName("존재하지 않는 영화 ID로 삭제 요청 시 예외가 발생한다")
        void fail_not_found() {
            // given
            Long invalidId = -1L;

            // when & then
            assertThatThrownBy(() -> movieService.deleteMovie(invalidId))
                    .isInstanceOf(MovieException.class)
                    .hasMessageContaining(MOVIE_NOT_FOUND.getMessage()); // 혹은 정의된 예외 메시지 키워드
        }
    }

    @Nested
    @DisplayName("상영중인 영화 조회")
    class GetMoviesWithScreenings {

        @Test
        @DisplayName("영화 제목과 장르로 조회하면 해당 영화와 관련된 상영 정보가 반환된다")
        void success() {
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
                .rating(Rating.R_19)
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