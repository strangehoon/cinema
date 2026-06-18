package com.example.movie.dto.response;

import com.example.db.entity.Movie;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class MovieUpdateServiceResponse {

    private Long movieId;
    private String title;
    private String rating;
    private String genre;
    private LocalDate releasedDate;
    private String thumbnailImage;
    private int runningTimeMin;
    private List<MovieUpdateServiceResponse.ScreeningResponse> screenings;

    public static MovieUpdateServiceResponse from(Movie movie) {
        return MovieUpdateServiceResponse.builder()
                .movieId(movie.getId())
                .title(movie.getTitle())
                .rating(String.valueOf(movie.getRating()))
                .genre(String.valueOf(movie.getGenre()))
                .releasedDate(movie.getReleasedDate())
                .thumbnailImage(movie.getThumbnailImage())
                .runningTimeMin(movie.getRunningTimeMin())
                .screenings(
                        movie.getScreenings().stream()
                                .map(screening -> ScreeningResponse.builder()
                                        .screeningId(screening.getId())
                                        .date(screening.getDate())
                                        .startedAt(screening.getStartedAt())
                                        .endedAt(screening.getEndedAt())
                                        .theaterId(screening.getTheater().getId())
                                        .build())
                                .toList()
                )
                .build();
    }

    @Getter
    @Builder
    public static class ScreeningResponse {
        private Long screeningId;
        private LocalDate date;
        private LocalDateTime startedAt;
        private LocalDateTime endedAt;
        private Long theaterId;
    }
}
