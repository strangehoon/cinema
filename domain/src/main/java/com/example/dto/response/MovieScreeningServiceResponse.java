package com.example.dto.response;

import com.example.entity.Movie;
import com.example.entity.Screening;
import com.example.entity.Theater;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Builder
@Getter
public class MovieScreeningServiceResponse {

    private String title;
    private String rating;
    private LocalDate releaseDate;
    private String thumbnailImage;
    private int runningTime;
    private String genre;
    private String theaterName;
    private List<ScreeningServiceResponse> screeningServiceResponses;

    public static MovieScreeningServiceResponse from(Movie movie, Theater theater) {
        List<ScreeningServiceResponse> screeningServiceRespons = movie.getScreenings().stream()
                .filter(screening -> !screening.getDate().isBefore(movie.getReleasedDate()))
                .sorted(Comparator.comparing(Screening::getStartedAt))
                .map(ScreeningServiceResponse::from)
                .collect(Collectors.toList());

        return MovieScreeningServiceResponse.builder()
                .title(movie.getTitle())
                .rating(movie.getRating().name())
                .releaseDate(movie.getReleasedDate())
                .thumbnailImage(movie.getThumbnailImage())
                .runningTime(movie.getRunningTime())
                .genre(movie.getGenre().name())
                .theaterName(theater.getName())
                .screeningServiceResponses(screeningServiceRespons)
                .build();
    }
}
