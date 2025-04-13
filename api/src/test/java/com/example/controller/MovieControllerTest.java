package com.example.controller;

import com.example.config.IntegrationControllerSupport;
import com.example.dto.response.MovieScreeningServiceResponse;
import com.example.dto.response.PageResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import java.time.LocalDate;
import java.util.List;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;


class MovieControllerTest extends IntegrationControllerSupport {

    @Nested
    @DisplayName("상영중인 영화 조회")
    class getMovies {

        @Test
        @DisplayName("영화 목록을 정상적으로 조회한다")
        void getMovies_success() throws Exception {
            // given
            MovieScreeningServiceResponse movie1 = MovieScreeningServiceResponse.builder()
                    .title("movie1")
                    .rating("R_19")
                    .releaseDate(LocalDate.of(2014, 11, 7))
                    .thumbnailImage("movie1.jpg")
                    .runningTime(169)
                    .genre("SF")
                    .screeningServiceResponses(List.of())
                    .build();

            PageImpl<MovieScreeningServiceResponse> page = new PageImpl<>(
                    List.of(movie1),
                    PageRequest.of(0, 10),
                    1
            );

            PageResponse<MovieScreeningServiceResponse> serviceResponse = PageResponse.of(List.of(movie1), page);

            given(movieService.getMoviesWithScreenings(any(), any(), anyInt(), anyInt()))
                    .willReturn(serviceResponse);

            // when & then
            mockMvc.perform(get("/movies")
                            .param("title", "movie1")
                            .param("genre", "SF")
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.status").value("OK"))
                    .andExpect(jsonPath("$.message").value("OK"))
                    .andExpect(jsonPath("$.data.content[0].title").value("movie1"))
                    .andExpect(jsonPath("$.data.content[0].rating").value("R_19"))
                    .andExpect(jsonPath("$.data.content[0].releaseDate").value("2014-11-07"))
                    .andExpect(jsonPath("$.data.content[0].thumbnailImage").value("movie1.jpg"))
                    .andExpect(jsonPath("$.data.content[0].runningTime").value(169))
                    .andExpect(jsonPath("$.data.content[0].genre").value("SF"));
        }
    }
}