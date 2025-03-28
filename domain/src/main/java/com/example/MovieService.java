package com.example;

import com.example.dto.MovieScreeningResponse;
import com.example.entity.Movie;
import com.example.entity.Screening;
import com.example.enums.Genre;
import com.example.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MovieService {

    private final MovieRepository movieRepository;

    @Cacheable(
            value = "movies",
            key = "#genre",
            condition = "#genre != null and #title == null", cacheManager = "contentCacheManager"
    )
    public List<MovieScreeningResponse> getMoviesWithScreenings(String title, String genre) {
        Genre genreEnum = genre != null ? Genre.valueOf(genre.toUpperCase()) : null;
        List<Movie> movies = movieRepository.searchMoviesWithScreenings(title, genreEnum);
        return movies.stream()
                .flatMap(movie -> movie.getScreenings().stream()
                        .collect(Collectors.groupingBy(Screening::getTheater))
                        .entrySet().stream()
                        .map(entry -> MovieScreeningResponse.from(movie, entry.getKey(), entry.getValue()))
                )
                .sorted(Comparator.comparing(response -> response.getScreeningTimes().get(0)))
                .collect(Collectors.toList());
    }
}
