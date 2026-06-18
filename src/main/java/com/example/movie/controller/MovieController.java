package com.example.movie.controller;

import com.example.movie.dto.request.MovieCreateRequest;
import com.example.movie.dto.request.MovieUpdateRequest;
import com.example.movie.dto.response.MovieCreateResponse;
import com.example.movie.dto.response.MovieUpdateResponse;
import com.example.movie.service.MovieService;
import com.example.common.ApiResponse;
import com.example.movie.dto.response.MovieScreeningResponse;
import com.example.movie.dto.response.MovieScreeningServiceResponse;
import com.example.common.dto.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;

    @PostMapping
    public ApiResponse<MovieCreateResponse> createMovie(@RequestBody @Valid MovieCreateRequest request) {
        return ApiResponse.ok(MovieCreateResponse.from(movieService.createMovie(request.toServiceRequest())));
    }

    @PutMapping("/{movieId}")
    public ApiResponse<MovieUpdateResponse> updateMovie(@PathVariable Long movieId, @RequestBody @Valid MovieUpdateRequest request) {
        return ApiResponse.ok(MovieUpdateResponse.from(movieService.updateMovie(movieId, request.toServiceRequest())));
    }

    @DeleteMapping("/{movieId}")
    public ApiResponse<Long> deleteMovie(@PathVariable Long movieId) {
        return ApiResponse.ok(movieService.deleteMovie(movieId));
    }

    @GetMapping
    public ApiResponse<PageResponse<MovieScreeningResponse>> getMoviesWithScreenings(
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