package com.example.movie.dto.response;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class MovieCreateResponse {

    private Long movieId;
    private String title;
    private String rating;
    private String genre;
    private LocalDate releasedDate;
    private String thumbnailImage;
    private int runningTimeMin;
    private List<ScreeningResponse> screenings;

    public static MovieCreateResponse from(MovieCreateServiceResponse serviceResponse){
        return MovieCreateResponse.builder()
                .movieId(serviceResponse.getMovieId())
                .title(serviceResponse.getTitle())
                .rating(serviceResponse.getRating())
                .genre(serviceResponse.getGenre())
                .releasedDate(serviceResponse.getReleasedDate())
                .thumbnailImage(serviceResponse.getThumbnailImage())
                .runningTimeMin(serviceResponse.getRunningTimeMin())
                .screenings(serviceResponse.getScreenings().stream()
                        .map(ScreeningResponse::from)
                        .toList())
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

        public static ScreeningResponse from(MovieCreateServiceResponse.ScreeningResponse response) {
            return ScreeningResponse.builder()
                    .screeningId(response.getScreeningId())
                    .date(response.getDate())
                    .startedAt(response.getStartedAt())
                    .endedAt(response.getEndedAt())
                    .theaterId(response.getTheaterId())
                    .build();
        }
    }
}
