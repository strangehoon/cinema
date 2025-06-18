package com.example.movie;

import com.example.config.IntegrationControllerSupport;
import com.example.movie.dto.request.MovieCreateRequest;
import com.example.movie.dto.request.MovieUpdateRequest;
import com.example.movie.dto.response.MovieCreateServiceResponse;
import com.example.movie.dto.response.MovieScreeningServiceResponse;
import com.example.common.dto.PageResponse;
import com.example.movie.dto.response.MovieUpdateServiceResponse;
import com.example.movie.dto.response.ScreeningServiceResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.MediaType;
import static org.mockito.BDDMockito.given;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.ArgumentMatchers.any;

class MovieControllerTest extends IntegrationControllerSupport {

    @Nested
    @DisplayName("영화 정보 등록")
    class CreateMovie {

        @Test
        @DisplayName("영화 정보를 정상적으로 등록한다")
        void success() throws Exception {
            // given
            MovieCreateRequest.ScreeningRequest screeningRequest = MovieCreateRequest.ScreeningRequest.builder()
                    .date(LocalDate.of(2025, 5, 13))
                    .startedAt(LocalDateTime.of(2025, 5, 13, 15, 0))
                    .endedAt(LocalDateTime.of(2025, 5, 13, 17, 30))
                    .theaterId(1L)
                    .build();

            MovieCreateRequest request = MovieCreateRequest.builder()
                    .title("movie1")
                    .rating("R_19")
                    .releasedDate(LocalDate.of(2014, 11, 7))
                    .thumbnailImage("movie1.jpg")
                    .runningTimeMin(169)
                    .genre("SF")
                    .screenings(List.of(screeningRequest))
                    .build();

            MovieCreateServiceResponse.ScreeningResponse screeningResponse = MovieCreateServiceResponse.ScreeningResponse.builder()
                    .screeningId(1L)
                    .date(LocalDate.of(2025, 5, 13))
                    .startedAt(LocalDateTime.of(2025, 5, 13, 15, 0))
                    .endedAt(LocalDateTime.of(2025, 5, 13, 17, 30))
                    .theaterId(1L)
                    .build();

            MovieCreateServiceResponse movieCreateResponse = MovieCreateServiceResponse.builder()
                    .movieId(1L)
                    .title("movie1")
                    .rating("R_19")
                    .releasedDate(LocalDate.of(2014, 11, 7))
                    .thumbnailImage("movie1.jpg")
                    .runningTimeMin(169)
                    .genre("SF")
                    .screenings(List.of(screeningResponse))
                    .build();

            given(movieService.createMovie(any())).willReturn(movieCreateResponse);

            // when & then
            mockMvc.perform(post("/movies")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.message").value("OK"))
                    .andExpect(jsonPath("$.data.movieId").value(1L))
                    .andExpect(jsonPath("$.data.title").value("movie1"))
                    .andExpect(jsonPath("$.data.rating").value("R_19"))
                    .andExpect(jsonPath("$.data.releasedDate").value("2014-11-07"))
                    .andExpect(jsonPath("$.data.thumbnailImage").value("movie1.jpg"))
                    .andExpect(jsonPath("$.data.runningTimeMin").value(169))
                    .andExpect(jsonPath("$.data.genre").value("SF"))
                    .andExpect(jsonPath("$.data.screenings[0].screeningId").value(1L))
                    .andExpect(jsonPath("$.data.screenings[0].date").value("2025-05-13"))
                    .andExpect(jsonPath("$.data.screenings[0].startedAt").value("2025-05-13T15:00:00"))
                    .andExpect(jsonPath("$.data.screenings[0].endedAt").value("2025-05-13T17:30:00"))
                    .andExpect(jsonPath("$.data.screenings[0].theaterId").value(1L));
        }

        @ParameterizedTest(name = "{index}: title={0}, rating={1}, releasedDate={2}, thumbnailImage={3}, runningTimeMin={4}, genre={5}, screenings={6}")
        @MethodSource("invalidMovieCreateRequests")
        @DisplayName("유효하지 않은 영화 생성 요청은 400 Bad Request를 반환한다")
        void fail_invalid_inputs(String title, String rating, LocalDate releasedDate,
                                            String thumbnailImage, int runningTimeMin,
                                            String genre, List<MovieCreateRequest.ScreeningRequest> screenings) throws Exception {
            // given
            MovieCreateRequest request = MovieCreateRequest.builder()
                    .title(title)
                    .rating(rating)
                    .releasedDate(releasedDate)
                    .thumbnailImage(thumbnailImage)
                    .runningTimeMin(runningTimeMin)
                    .genre(genre)
                    .screenings(screenings)
                    .build();

            // when & then
            mockMvc.perform(post("/movies")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        static Stream<Arguments> invalidMovieCreateRequests() {
            MovieCreateRequest.ScreeningRequest validScreening = MovieCreateRequest.ScreeningRequest.builder()
                    .date(LocalDate.of(2025, 5, 13))
                    .startedAt(LocalDateTime.of(2025, 5, 13, 15, 0))
                    .endedAt(LocalDateTime.of(2025, 5, 13, 17, 30))
                    .theaterId(1L)
                    .build();

            return Stream.of(
                    // title null
                    Arguments.of(null, "R_19", LocalDate.now(), "thumb.jpg", 100, "SF", List.of(validScreening)),
                    // title blank
                    Arguments.of("  ", "R_19", LocalDate.now(), "thumb.jpg", 100, "SF", List.of(validScreening)),
                    // title too long
                    Arguments.of("m".repeat(31), "R_19", LocalDate.now(), "thumb.jpg", 100, "SF", List.of(validScreening)),

                    // rating null
                    Arguments.of("movie", null, LocalDate.now(), "thumb.jpg", 100, "SF", List.of(validScreening)),
                    // rating empty
                    Arguments.of("movie", "", LocalDate.now(), "thumb.jpg", 100, "SF", List.of(validScreening)),
                    // rating too long
                    Arguments.of("movie", "R".repeat(21), LocalDate.now(), "thumb.jpg", 100, "SF", List.of(validScreening)),

                    // releasedDate null
                    Arguments.of("movie", "R_19", null, "thumb.jpg", 100, "SF", List.of(validScreening)),

                    // thumbnailImage null
                    Arguments.of("movie", "R_19", LocalDate.now(), null, 100, "SF", List.of(validScreening)),
                    // thumbnailImage too long
                    Arguments.of("movie", "R_19", LocalDate.now(), "x".repeat(51), 100, "SF", List.of(validScreening)),

                    // runningTimeMin negative
                    Arguments.of("movie", "R_19", LocalDate.now(), "thumb.jpg", -10, "SF", List.of(validScreening)),

                    // genre null
                    Arguments.of("movie", "R_19", LocalDate.now(), "thumb.jpg", 100, null, List.of(validScreening)),
                    // genre empty
                    Arguments.of("movie", "R_19", LocalDate.now(), "thumb.jpg", 100, "", List.of(validScreening)),
                    // genre too long
                    Arguments.of("movie", "R_19", LocalDate.now(), "thumb.jpg", 100, "x".repeat(21), List.of(validScreening)),

                    // screenings null
                    Arguments.of("movie", "R_19", LocalDate.now(), "thumb.jpg", 100, "SF", null),
                    // screenings empty
                    Arguments.of("movie", "R_19", LocalDate.now(), "thumb.jpg", 100, "SF", List.of())
            );
        }
    }

    @Nested
    @DisplayName("영화 정보 수정")
    class UpdateMovie {

        @Test
        @DisplayName("영화 정보를 정상적으로 수정한다")
        void success() throws Exception {
            // given
            Long movieId = 1L;
            MovieUpdateRequest.ScreeningRequest screeningRequest = MovieUpdateRequest.ScreeningRequest.builder()
                    .date(LocalDate.of(2025, 5, 13))
                    .startedAt(LocalDateTime.of(2025, 5, 13, 15, 0))
                    .endedAt(LocalDateTime.of(2025, 5, 13, 17, 30))
                    .theaterId(1L)
                    .build();

            MovieUpdateRequest request = MovieUpdateRequest.builder()
                    .title("movie1")
                    .rating("R_19")
                    .releasedDate(LocalDate.of(2014, 11, 7))
                    .thumbnailImage("movie1.jpg")
                    .runningTimeMin(169)
                    .genre("SF")
                    .screenings(List.of(screeningRequest))
                    .build();

            MovieUpdateServiceResponse.ScreeningResponse screeningResponse = MovieUpdateServiceResponse.ScreeningResponse.builder()
                    .screeningId(1L)
                    .date(LocalDate.of(2025, 5, 13))
                    .startedAt(LocalDateTime.of(2025, 5, 13, 15, 0))
                    .endedAt(LocalDateTime.of(2025, 5, 13, 17, 30))
                    .theaterId(1L)
                    .build();

            MovieUpdateServiceResponse movieUpdateResponse = MovieUpdateServiceResponse.builder()
                    .movieId(1L)
                    .title("movie1")
                    .rating("R_19")
                    .releasedDate(LocalDate.of(2014, 11, 7))
                    .thumbnailImage("movie1.jpg")
                    .runningTimeMin(169)
                    .genre("SF")
                    .screenings(List.of(screeningResponse))
                    .build();

            given(movieService.updateMovie(any(), any())).willReturn(movieUpdateResponse);

            // when & then
            mockMvc.perform(put("/movies/{movieId}", movieId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.message").value("OK"))
                    .andExpect(jsonPath("$.data.movieId").value(1L))
                    .andExpect(jsonPath("$.data.title").value("movie1"))
                    .andExpect(jsonPath("$.data.rating").value("R_19"))
                    .andExpect(jsonPath("$.data.releasedDate").value("2014-11-07"))
                    .andExpect(jsonPath("$.data.thumbnailImage").value("movie1.jpg"))
                    .andExpect(jsonPath("$.data.runningTimeMin").value(169))
                    .andExpect(jsonPath("$.data.genre").value("SF"))
                    .andExpect(jsonPath("$.data.screenings[0].screeningId").value(1L))
                    .andExpect(jsonPath("$.data.screenings[0].date").value("2025-05-13"))
                    .andExpect(jsonPath("$.data.screenings[0].startedAt").value("2025-05-13T15:00:00"))
                    .andExpect(jsonPath("$.data.screenings[0].endedAt").value("2025-05-13T17:30:00"))
                    .andExpect(jsonPath("$.data.screenings[0].theaterId").value(1L));
        }

        @ParameterizedTest(name = "{index}: title={0}, rating={1}, releasedDate={2}, thumbnailImage={3}, runningTimeMin={4}, genre={5}, screenings={6}")
        @MethodSource("invalidMovieCreateRequests")
        @DisplayName("유효하지 않은 영화 생성 요청은 400 Bad Request를 반환한다")
        void fail_invalid_inputs(String title, String rating, LocalDate releasedDate,
                                            String thumbnailImage, int runningTimeMin,
                                            String genre, List<MovieCreateRequest.ScreeningRequest> screenings) throws Exception {
            // given
            MovieCreateRequest request = MovieCreateRequest.builder()
                    .title(title)
                    .rating(rating)
                    .releasedDate(releasedDate)
                    .thumbnailImage(thumbnailImage)
                    .runningTimeMin(runningTimeMin)
                    .genre(genre)
                    .screenings(screenings)
                    .build();

            // when & then
            mockMvc.perform(post("/movies")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        static Stream<Arguments> invalidMovieCreateRequests() {
            MovieCreateRequest.ScreeningRequest validScreening = MovieCreateRequest.ScreeningRequest.builder()
                    .date(LocalDate.of(2025, 5, 13))
                    .startedAt(LocalDateTime.of(2025, 5, 13, 15, 0))
                    .endedAt(LocalDateTime.of(2025, 5, 13, 17, 30))
                    .theaterId(1L)
                    .build();

            return Stream.of(
                    // title null
                    Arguments.of(null, "R_19", LocalDate.now(), "thumb.jpg", 100, "SF", List.of(validScreening)),
                    // title blank
                    Arguments.of("  ", "R_19", LocalDate.now(), "thumb.jpg", 100, "SF", List.of(validScreening)),
                    // title too long
                    Arguments.of("m".repeat(31), "R_19", LocalDate.now(), "thumb.jpg", 100, "SF", List.of(validScreening)),

                    // rating null
                    Arguments.of("movie", null, LocalDate.now(), "thumb.jpg", 100, "SF", List.of(validScreening)),
                    // rating empty
                    Arguments.of("movie", "", LocalDate.now(), "thumb.jpg", 100, "SF", List.of(validScreening)),
                    // rating too long
                    Arguments.of("movie", "R".repeat(21), LocalDate.now(), "thumb.jpg", 100, "SF", List.of(validScreening)),

                    // releasedDate null
                    Arguments.of("movie", "R_19", null, "thumb.jpg", 100, "SF", List.of(validScreening)),

                    // thumbnailImage null
                    Arguments.of("movie", "R_19", LocalDate.now(), null, 100, "SF", List.of(validScreening)),
                    // thumbnailImage too long
                    Arguments.of("movie", "R_19", LocalDate.now(), "x".repeat(51), 100, "SF", List.of(validScreening)),

                    // runningTimeMin negative
                    Arguments.of("movie", "R_19", LocalDate.now(), "thumb.jpg", -10, "SF", List.of(validScreening)),

                    // genre null
                    Arguments.of("movie", "R_19", LocalDate.now(), "thumb.jpg", 100, null, List.of(validScreening)),
                    // genre empty
                    Arguments.of("movie", "R_19", LocalDate.now(), "thumb.jpg", 100, "", List.of(validScreening)),
                    // genre too long
                    Arguments.of("movie", "R_19", LocalDate.now(), "thumb.jpg", 100, "x".repeat(21), List.of(validScreening)),

                    // screenings null
                    Arguments.of("movie", "R_19", LocalDate.now(), "thumb.jpg", 100, "SF", null),
                    // screenings empty
                    Arguments.of("movie", "R_19", LocalDate.now(), "thumb.jpg", 100, "SF", List.of())
            );
        }
    }

    @Nested
    @DisplayName("영화 정보 삭제")
    class DeleteMovie {

        @Test
        @DisplayName("영화 정보를 정상적으로 삭제한다")
        void success() throws Exception {
            // given
            Long movieId = 1L;
            given(movieService.deleteMovie(movieId)).willReturn(movieId);

            // when & then
            mockMvc.perform(delete("/movies/{movieId}", movieId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.message").value("OK"))
                    .andExpect(jsonPath("$.data").value(movieId));
        }
    }

    @Nested
    @DisplayName("상영중인 영화 조회")
    class GetMovies {

        @Test
        @DisplayName("영화 목록을 정상적으로 조회한다")
        void success() throws Exception {
            // given
            ScreeningServiceResponse screening = ScreeningServiceResponse.builder()
                    .screeningId(1L)
                    .date(LocalDate.of(2025, 5, 13))
                    .startedAt(LocalDateTime.of(2025, 5, 13, 15, 0))
                    .endedAt(LocalDateTime.of(2025, 5, 13, 17, 30))
                    .theaterName("theater1")
                    .build();

            MovieScreeningServiceResponse movie = MovieScreeningServiceResponse.builder()
                    .title("movie1")
                    .rating("R_19")
                    .releaseDate(LocalDate.of(2010, 7, 21))
                    .thumbnailImage("movie1.jpg")
                    .runningTime(148)
                    .genre("SF")
                    .screeningServiceResponses(List.of(screening))
                    .build();

            PageResponse<MovieScreeningServiceResponse> servicePage = PageResponse.<MovieScreeningServiceResponse>builder()
                    .content(List.of(movie))
                    .page(0)
                    .size(10)
                    .totalElements(1)
                    .totalPages(1)
                    .hasNext(false)
                    .isLast(true)
                    .build();

            given(movieService.getMoviesWithScreenings("movie1", "SF", 0, 10))
                    .willReturn(servicePage);

            // when & then
            mockMvc.perform(get("/movies")
                            .param("title", "movie1")
                            .param("genre", "SF")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.message").value("OK"))
                    .andExpect(jsonPath("$.data.content[0].title").value("movie1"))
                    .andExpect(jsonPath("$.data.content[0].rating").value("R_19"))
                    .andExpect(jsonPath("$.data.content[0].releaseDate").value("2010-07-21"))
                    .andExpect(jsonPath("$.data.content[0].thumbnailImage").value("movie1.jpg"))
                    .andExpect(jsonPath("$.data.content[0].runningTime").value(148))
                    .andExpect(jsonPath("$.data.content[0].genre").value("SF"))
                    .andExpect(jsonPath("$.data.content[0].screeningResponses[0].screeningId").value(1L))
                    .andExpect(jsonPath("$.data.content[0].screeningResponses[0].date").value("2025-05-13"))
                    .andExpect(jsonPath("$.data.content[0].screeningResponses[0].startedAt").value("2025-05-13T15:00:00"))
                    .andExpect(jsonPath("$.data.content[0].screeningResponses[0].endedAt").value("2025-05-13T17:30:00"))
                    .andExpect(jsonPath("$.data.content[0].screeningResponses[0].theaterName").value("theater1"))
                    .andExpect(jsonPath("$.data.page").value(0))
                    .andExpect(jsonPath("$.data.size").value(10))
                    .andExpect(jsonPath("$.data.totalElements").value(1))
                    .andExpect(jsonPath("$.data.totalPages").value(1))
                    .andExpect(jsonPath("$.data.hasNext").value(false))
                    .andExpect(jsonPath("$.data.last").value(true));
        }
    }
}