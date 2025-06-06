package com.example.movie.service;

import com.example.common.PERCacheable;
import com.example.db.entity.Screening;
import com.example.db.entity.Theater;
import com.example.db.repository.TheaterRepository;
import com.example.movie.dto.request.MovieCreateServiceRequest;
import com.example.movie.dto.request.MovieUpdateServiceRequest;
import com.example.movie.dto.response.MovieScreeningServiceResponse;
import com.example.common.PageResponse;
import com.example.db.entity.Movie;
import com.example.db.enums.Genre;
import com.example.db.repository.MovieRepository;
import com.example.movie.dto.response.MovieCreateServiceResponse;
import com.example.movie.dto.response.MovieUpdateServiceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import java.util.*;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MovieService {

    private final MovieRepository movieRepository;
    private final TheaterRepository theaterRepository;

    public MovieCreateServiceResponse createMovie(MovieCreateServiceRequest movieCreateServiceRequest){
        Movie movie = Movie.of(movieCreateServiceRequest.getTitle(), movieCreateServiceRequest.getRating(),
                movieCreateServiceRequest.getReleasedDate(), movieCreateServiceRequest.getThumbnailImage(),
                movieCreateServiceRequest.getRunningTimeMin(), movieCreateServiceRequest.getGenre());

        for(MovieCreateServiceRequest.ScreeningServiceCreateRequest screeningServiceCreateRequest : movieCreateServiceRequest.getScreenings()){
            Theater theater = theaterRepository.findById(screeningServiceCreateRequest.getTheaterId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 극장 정보입니다."));

            Screening screening = Screening.of(screeningServiceCreateRequest.getDate(),screeningServiceCreateRequest.getStartedAt(),
                    screeningServiceCreateRequest.getEndedAt(), movie, theater);

            movie.addScreening(screening);
        }

        Movie savedMovie = movieRepository.save(movie);
        return MovieCreateServiceResponse.from(savedMovie);
    }

    public MovieUpdateServiceResponse updateMovie(Long movieId, MovieUpdateServiceRequest movieUpdateServiceRequest){
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 영화입니다."));

        movie.updateInfo(movieUpdateServiceRequest.getTitle(), movieUpdateServiceRequest.getRating(), movieUpdateServiceRequest.getReleasedDate(),
                movieUpdateServiceRequest.getThumbnailImage(), movieUpdateServiceRequest.getRunningTimeMin(), movieUpdateServiceRequest.getGenre());

        movie.clearScreenings();

        for (MovieUpdateServiceRequest.ScreeningServiceUpdateRequest screeningServiceUpdateRequest : movieUpdateServiceRequest.getScreenings()) {
            Theater theater = theaterRepository.findById(screeningServiceUpdateRequest.getTheaterId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 극장 정보입니다."));

            Screening screening = Screening.of(screeningServiceUpdateRequest.getDate(),screeningServiceUpdateRequest.getStartedAt(),
                    screeningServiceUpdateRequest.getEndedAt(), movie, theater);
            movie.addScreening(screening);
        }

        return MovieUpdateServiceResponse.from(movie);
    }

    public Long deleteMovie(Long movieId){
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 영화입니다."));
        movieRepository.delete(movie);
        return movie.getId();
    }

    @PERCacheable(
            key = "#genre != null ? #genre + '_page_' + #page : 'all_page_' + #page",
            condition = "(#genre != null and #title == null and #page >= 0 and #page < 2) " +
                    "|| (#genre == null and #title == null and #page >= 0 and #page < 2)",
            ttl = 300
    )
    @Transactional(readOnly = true)
    public PageResponse<MovieScreeningServiceResponse> getMoviesWithScreenings1(String title, String genre, int page, int size) {

        Genre genreEnum = genre != null ? Genre.valueOf(genre.toUpperCase()) : null;

        Page<Movie> moviePage = movieRepository.searchMoviesWithScreenings(
                title,
                genreEnum,
                PageRequest.of(page, size)
        );
        System.out.println("!!");
        List<MovieScreeningServiceResponse> content = moviePage.getContent().stream()
                .sorted(Comparator.comparing(Movie::getReleasedDate).reversed())
                .map(MovieScreeningServiceResponse::from)
                .toList();

        return PageResponse.of(content, moviePage);
    }

    @Cacheable(
            value = "movies",
            key = "#genre != null ? #genre + '_page_' + #page : 'all_page_' + #page",
            condition = "(#genre != null and #title == null and #page >= 0 and #page < 2) " +
                    "|| (#genre == null and #title == null and #page >= 0 and #page < 2)",
            cacheManager = "contentCacheManager"
            )
    @Transactional(readOnly = true)
    public PageResponse<MovieScreeningServiceResponse> getMoviesWithScreenings2(String title, String genre, int page, int size) {

        Genre genreEnum = genre != null ? Genre.valueOf(genre.toUpperCase()) : null;

        Page<Movie> moviePage = movieRepository.searchMoviesWithScreenings(
                title,
                genreEnum,
                PageRequest.of(page, size)
        );
        System.out.println("!!");
        List<MovieScreeningServiceResponse> content = moviePage.getContent().stream()
                .sorted(Comparator.comparing(Movie::getReleasedDate).reversed())
                .map(MovieScreeningServiceResponse::from)
                .toList();

        return PageResponse.of(content, moviePage);
    }

    @Cacheable(
            value = "movies",
            key = "#genre != null ? #genre + '_page_' + #page : 'all_page_' + #page",
            condition = "(#genre != null and #title == null and #page >= 0 and #page < 2) " +
                    "|| (#genre == null and #title == null and #page >= 0 and #page < 2)",
            cacheManager = "contentCacheManager",
            sync = true
    )
    @Transactional(readOnly = true)
    public PageResponse<MovieScreeningServiceResponse> getMoviesWithScreenings3(String title, String genre, int page, int size) {

        Genre genreEnum = genre != null ? Genre.valueOf(genre.toUpperCase()) : null;

        Page<Movie> moviePage = movieRepository.searchMoviesWithScreenings(
                title,
                genreEnum,
                PageRequest.of(page, size)
        );
        System.out.println("!!");
        List<MovieScreeningServiceResponse> content = moviePage.getContent().stream()
                .sorted(Comparator.comparing(Movie::getReleasedDate).reversed())
                .map(MovieScreeningServiceResponse::from)
                .toList();

        return PageResponse.of(content, moviePage);
    }
}
