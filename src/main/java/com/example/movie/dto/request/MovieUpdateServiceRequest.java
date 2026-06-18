package com.example.movie.dto.request;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class MovieUpdateServiceRequest {

    private String title;

    private String rating;

    private LocalDate releasedDate;

    private String thumbnailImage;

    private Integer runningTimeMin;

    private String genre;

    private List<ScreeningServiceUpdateRequest> screenings;

    @Getter
    @Builder
    public static class ScreeningServiceUpdateRequest {

        private LocalDate date;

        private LocalDateTime startedAt;

        private LocalDateTime endedAt;

        private Long theaterId;
    }
}