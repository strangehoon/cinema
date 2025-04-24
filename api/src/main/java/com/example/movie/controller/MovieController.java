package com.example.movie.controller;

import com.example.movie.service.MovieService;
import com.example.common.ApiResponse;
import com.example.movie.dto.response.MovieScreeningResponse;
import com.example.movie.dto.response.MovieScreeningServiceResponse;
import com.example.common.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;

    @GetMapping("/movies")
    public ApiResponse<PageResponse<MovieScreeningResponse>> getMovies(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String genre,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PageResponse<MovieScreeningServiceResponse> serviceResult =
                movieService.getMoviesWithScreenings(title, genre, page, size);

        return ApiResponse.ok(PageResponse.from(serviceResult, MovieScreeningResponse::from));
    }
}