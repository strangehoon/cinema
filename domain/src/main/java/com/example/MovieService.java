package com.example;

import com.example.dto.MovieScreeningResponse;
import com.example.entity.Movie;
import com.example.enums.Genre;
import com.example.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.cache.annotation.Cacheable;

@Service
@RequiredArgsConstructor
public class MovieService {

    private final MovieRepository movieRepository;

    @Cacheable(
            value = "movies",
            key = "#genre != null ? #genre : 'ALL'",
            condition = "#title == null",
            cacheManager = "contentCacheManager"
    )
    public List<MovieScreeningResponse> getMoviesWithScreenings(String title, String genre) {
        Genre genreEnum = null;
        if (genre != null) {
            genreEnum = Genre.valueOf(genre.toUpperCase());
        }

        List<Movie> movies = movieRepository.searchMoviesWithScreenings(title, genreEnum);

        return movies.stream()
                .flatMap(movie -> MovieScreeningResponse.from(movie).stream()) // 🎯 극장별로 분리된 JSON 리스트 반환
                .collect(Collectors.toList());
    }
}
