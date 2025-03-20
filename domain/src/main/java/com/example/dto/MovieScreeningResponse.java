package com.example.dto;

import com.example.entity.Movie;
import com.example.entity.Screening;
import com.example.enums.Genre;
import com.example.enums.Rating;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Builder
@Getter
public class MovieScreeningResponse {

    private String title;
    private String rating;
    private LocalDateTime releaseDate;
    private String thumbnailImage;
    private int runningTime;
    private String genre;
    private String theaterName;
    private List<String> screeningTimes;

    public static MovieScreeningResponse of(Movie movie, List<Screening> screenings) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        return MovieScreeningResponse.builder()
                .title(movie.getTitle())
                .rating(movie.getRating().getValue())
                .releaseDate(movie.getReleaseDate())
                .thumbnailImage(movie.getThumbnailImage())
                .runningTime(movie.getRunningTime())
                .genre(movie.getGenre().getValue())
                .theaterName(screenings.get(0).getTheater().getName())
                .screeningTimes(screenings.stream()
                        .sorted(Comparator.comparing(Screening::getStartTime))
                        .map(screening -> {
                            LocalDateTime startTime = screening.getStartTime();
                            LocalDateTime endTime = screening.getEndTime();
                            return startTime.format(formatter) + " : " + endTime.format(formatter);
                        })
                        .sorted()
                        .collect(Collectors.toList()))
                .build();
    }
}
