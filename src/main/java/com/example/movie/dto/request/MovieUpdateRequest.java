package com.example.movie.dto.request;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class MovieUpdateRequest {

    @NotBlank(message = "영화 제목은 필수입니다.")
    @Size(max = 30, message = "영화 제목은 30자 이내여야 합니다.")
    private String title;

    @NotBlank(message = "영화 등급은 필수입니다.")
    @Size(max = 20, message = "영화 등급은 20자 이내여야 합니다.")
    private String rating;

    @NotNull(message = "영화 개봉일은 필수입니다.")
    private LocalDate releasedDate;

    @NotBlank(message = "영화 썸네일 이미지는 필수입니다.")
    @Size(max = 50, message = "썸네일 이미지는 50자 이내여야 합니다.")
    private String thumbnailImage;

    @NotNull(message = "영화 상영 시간은 필수입니다.")
    @Positive(message = "영화 상영 시간은 양수여야 합니다.")
    private int runningTimeMin;

    @NotBlank(message = "영화 장르는 필수입니다.")
    @Size(max = 20, message = "영화 장르는 20자 이내여야 합니다.")
    private String genre;

    @NotEmpty(message = "상영 일정은 1개 이상 포함되어야 합니다.")
    private List<ScreeningRequest> screenings;

    public MovieUpdateServiceRequest toServiceRequest() {
        return MovieUpdateServiceRequest.builder()
                .title(title)
                .rating(rating)
                .releasedDate(releasedDate)
                .thumbnailImage(thumbnailImage)
                .runningTimeMin(runningTimeMin)
                .genre(genre)
                .screenings(
                        screenings.stream()
                                .map(ScreeningRequest::toServiceRequest)
                                .collect(Collectors.toList())
                )
                .build();
    }

    @Getter
    @Builder
    public static class ScreeningRequest {

        @NotNull(message = "상영 일자는 필수입니다.")
        private LocalDate date;

        @NotNull(message = "상영 시작 시간은 필수입니다.")
        private LocalDateTime startedAt;

        @NotNull(message = "상영 종료 시간은 필수입니다.")
        private LocalDateTime endedAt;

        @NotNull(message = "극장 ID는 필수입니다.")
        @Positive(message = "극장 ID는 양수여야 합니다.")
        private Long theaterId;

        public MovieUpdateServiceRequest.ScreeningServiceUpdateRequest toServiceRequest() {
            return MovieUpdateServiceRequest.ScreeningServiceUpdateRequest.builder()
                    .date(date)
                    .startedAt(startedAt)
                    .endedAt(endedAt)
                    .theaterId(theaterId)
                    .build();
        }
    }
}
