package com.example;

import com.example.dto.response.MovieScreeningServiceResponse;
import com.example.dto.response.PageResponse;
import com.example.entity.Movie;
import com.example.enums.Genre;
import com.example.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import java.util.*;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MovieService {

    private final MovieRepository movieRepository;

    @Cacheable(
            value = "movies",
            key = "#genre != null ? #genre : 'all'",
            condition = "(#genre != null and #title == null and #page == 0) || (#genre == null and #title == null and #page == 0)",
            cacheManager = "contentCacheManager"
    )
    @Transactional(readOnly = true)
    public PageResponse<MovieScreeningServiceResponse> getMoviesWithScreenings(String title, String genre, int page, int size) {

        Genre genreEnum = genre != null ? Genre.valueOf(genre.toUpperCase()) : null;

        Page<Movie> moviePage = movieRepository.searchMoviesWithScreenings(
                title,
                genreEnum,
                PageRequest.of(page, size)
        );

        List<MovieScreeningServiceResponse> content = moviePage.getContent().stream()
                .map(movie -> MovieScreeningServiceResponse.from(movie))
                .toList();

        return PageResponse.of(content, moviePage);
    }
}
