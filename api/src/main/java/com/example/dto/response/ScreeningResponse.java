package com.example.dto.response;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class ScreeningResponse {

    private final Long screeningId;
    private final LocalDate date;
    private final LocalDateTime startedAt;
    private final LocalDateTime endedAt;

    public static ScreeningResponse from(ScreeningServiceResponse serviceDto) {
        return ScreeningResponse.builder()
                .screeningId(serviceDto.getScreeningId())
                .date(serviceDto.getDate())
                .startedAt(serviceDto.getStartedAt())
                .endedAt(serviceDto.getEndedAt())
                .build();
    }

    public static List<ScreeningResponse> from(List<ScreeningServiceResponse> serviceDtos) {
        return serviceDtos.stream()
                .map(ScreeningResponse::from)
                .collect(Collectors.toList());
    }
}
