package com.example.dto;

import com.example.entity.Movie;
import com.example.entity.Screening;
import com.example.entity.Theater;
import com.fasterxml.jackson.annotation.JsonFormat;
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
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime releaseDate;
    private String thumbnailImage;
    private int runningTime;
    private String genre;
    private String theaterName;
    private List<String> screeningTimes;

    public static MovieScreeningResponse from(Movie movie, Theater theater, List<Screening> screenings) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        List<String> sortedScreeningTimes = screenings.stream()
                .sorted(Comparator.comparing(Screening::getStartTime))
                .map(screening -> {
                    LocalDateTime startTime = screening.getStartTime();
                    LocalDateTime endTime = screening.getEndTime();
                    return startTime.format(formatter) + " : " + endTime.format(formatter);
                })
                .collect(Collectors.toList());

        return MovieScreeningResponse.builder()
                .title(movie.getTitle())
                .rating(movie.getRating().getValue())
                .releaseDate(movie.getReleaseDate())
                .thumbnailImage(movie.getThumbnailImage())
                .runningTime(movie.getRunningTime())
                .genre(movie.getGenre().getValue())
                .theaterName(theater.getName())  // 극장명 정확히 매핑
                .screeningTimes(sortedScreeningTimes)
                .build();
    }
}
