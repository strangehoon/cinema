package com.example;

import com.example.dto.response.MovieScreeningServiceResponse;
import com.example.dto.response.PageResponse;
import com.example.entity.Movie;
import com.example.entity.Theater;
import com.example.enums.Genre;
import com.example.repository.MovieRepository;
import com.example.repository.TheaterRepository;
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
    private final TheaterRepository theaterRepository;


    @Cacheable(
            value = "movies",
            key = "#genre",
            condition = "#genre != null and #title == null", cacheManager = "contentCacheManager"
    )
    @Transactional(readOnly = true)
    public PageResponse<MovieScreeningServiceResponse> getMoviesWithScreenings(String title, String genre, Long theaterId, int page, int size) {
        Genre genreEnum = genre != null ? Genre.valueOf(genre.toUpperCase()) : null;

        Theater theater = theaterRepository.findById(theaterId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 극장이 존재하지 않습니다: "));

        Page<Movie> moviePage = movieRepository.searchMoviesWithScreenings(
                title,
                genreEnum,
                theaterId,
                PageRequest.of(page, size)
        );

        List<MovieScreeningServiceResponse> content = moviePage.getContent().stream()
                .map(movie -> MovieScreeningServiceResponse.from(movie, theater))
                .toList();

        return PageResponse.of(content, moviePage);
    }
}
