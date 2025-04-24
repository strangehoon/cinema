package com.example.movie.dto.response;

import com.example.entity.Screening;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ScreeningServiceResponse {

    private Long screeningId;
    private LocalDate date;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private String theaterName;

    public static ScreeningServiceResponse from(Screening screening) {
        return ScreeningServiceResponse.builder()
                .screeningId(screening.getId())
                .date(screening.getDate())
                .startedAt(screening.getStartedAt())
                .endedAt(screening.getEndedAt())
                .theaterName(screening.getTheater().getName())
                .build();
    }
}