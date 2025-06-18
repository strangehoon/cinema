package com.example.reservation.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReservationCreateResponse {
    private String orderId;
    private String orderName;
    Long totalAmount;

    public static ReservationCreateResponse from(ReservationCreateServiceResponse serviceDto){
        return ReservationCreateResponse.builder()
                .orderId(serviceDto.getOrderId())
                .orderName(serviceDto.getOrderName())
                .totalAmount(serviceDto.getTotalAmount())
                .build();
    }
}