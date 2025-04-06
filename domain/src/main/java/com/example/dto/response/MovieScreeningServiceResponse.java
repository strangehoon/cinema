package com.example.dto.response;

import com.example.entity.Movie;
import com.example.entity.Screening;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MovieScreeningServiceResponse {

    private String title;
    private String rating;
    private LocalDate releaseDate;
    private String thumbnailImage;
    private int runningTime;
    private String genre;
    private List<ScreeningServiceResponse> screeningServiceResponses;

    public static MovieScreeningServiceResponse from(Movie movie) {
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
                .runningTime(movie.getRunningTimeMin())
                .genre(movie.getGenre().name())
                .screeningServiceResponses(screeningServiceRespons)
                .build();
    }
}
