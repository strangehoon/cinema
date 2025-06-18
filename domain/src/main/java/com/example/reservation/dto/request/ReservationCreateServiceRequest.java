package com.example.reservation.dto.request;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class ReservationServiceRequest {

    private Long userId;
    private Long screeningId;
    private List<Long> seatIds;

    public List<String> toLockKeys() {
        return seatIds.stream()
                .map(seatId -> screeningId + ":" + seatId)
                .sorted()
                .toList();
    }
}