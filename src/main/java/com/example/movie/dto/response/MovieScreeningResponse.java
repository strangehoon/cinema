package com.example.movie.dto.response;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class MovieScreeningResponse {

    private final String title;
    private final String rating;
    private final LocalDate releaseDate;
    private final String thumbnailImage;
    private final int runningTime;
    private final String genre;
    private final List<ScreeningResponse> screeningResponses;

    public static MovieScreeningResponse from(MovieScreeningServiceResponse serviceDto) {
        return MovieScreeningResponse.builder()
                .title(serviceDto.getTitle())
                .rating(serviceDto.getRating())
                .releaseDate(serviceDto.getReleaseDate())
                .thumbnailImage(serviceDto.getThumbnailImage())
                .runningTime(serviceDto.getRunningTime())
                .genre(serviceDto.getGenre())
                .screeningResponses(ScreeningResponse.from(serviceDto.getScreeningServiceResponses()))
                .build();
    }
}
