package com.example.event.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReservationCompletedEvent {
    private String userName;
    private int seatCount;

    public static ReservationCompletedEvent of(String name, int seatCount){
        return ReservationCompletedEvent.builder()
                .userName(name)
                .seatCount(seatCount)
                .build();
    }
}