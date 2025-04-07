package com.example.dto.response;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class ReservationResponse {
    private final Long userId;
    private final Long screeningId;
    private final List<Long> reservedSeatIds;

    public static ReservationResponse from(ReservationServiceResponse serviceDto){
        return ReservationResponse.builder()
                .userId(serviceDto.getUserId())
                .screeningId(serviceDto.getScreeningId())
                .reservedSeatIds(serviceDto.getReservedSeatIds())
                .build();
    }
}