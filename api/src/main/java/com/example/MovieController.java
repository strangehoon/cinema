package com.example;

import com.example.dto.MovieScreeningResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;

    @GetMapping("/movies")
    public List<MovieScreeningResponse> getMovies(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String genre) {
        return movieService.getMoviesWithScreenings(title, genre);
    }
}
