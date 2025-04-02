package com.example.dto.request;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class ReservationServiceRequest {

    private Long memberId;
    private Long screeningId;
    private List<Long> seatIds;

    @Builder
    public ReservationServiceRequest(Long memberId, Long screeningId, List<Long> seatIds) {
        this.memberId = memberId;
        this.screeningId = screeningId;
        this.seatIds = seatIds;
    }
}
