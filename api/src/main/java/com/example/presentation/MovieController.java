package com.example.presentation;

import com.example.MovieService;
import com.example.dto.response.MovieScreeningResponse;
import com.example.dto.response.MovieScreeningServiceResponse;
import com.example.dto.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;

    @GetMapping("/movies")
    public PageResponse<MovieScreeningResponse> getMovies(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String genre,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PageResponse<MovieScreeningServiceResponse> serviceResult =
                movieService.getMoviesWithScreenings(title, genre, page, size);

        return PageResponse.from(serviceResult, MovieScreeningResponse::from);
    }
}