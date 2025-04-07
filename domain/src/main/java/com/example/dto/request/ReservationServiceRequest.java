package com.example.dto.request;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class ReservationServiceRequest {

    private Long memberId;
    private Long screeningId;
    private List<Long> seatIds;
}
