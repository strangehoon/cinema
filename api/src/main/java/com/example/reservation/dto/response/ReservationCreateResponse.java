package com.example.reservation.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReservationResponse {
    private String orderId;
    private String orderName;
    Long totalAmount;

    public static ReservationResponse from(ReservationServiceResponse serviceDto){
        return ReservationResponse.builder()
                .orderId(serviceDto.getOrderId())
                .orderName(serviceDto.getOrderName())
                .totalAmount(serviceDto.getTotalAmount())
                .build();
    }
}