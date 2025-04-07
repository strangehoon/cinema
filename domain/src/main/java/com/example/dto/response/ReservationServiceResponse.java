package com.example.dto.response;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class ReservationServiceResponse {
    private final Long userId;
    private final Long screeningId;
    private final List<Long> reservedSeatIds;

    public static ReservationServiceResponse from(ReservationServiceResponse serviceDto){
        return ReservationServiceResponse.builder()
                .userId(serviceDto.userId)
                .screeningId(serviceDto.screeningId)
                .reservedSeatIds(serviceDto.reservedSeatIds)
                .build();
    }
}