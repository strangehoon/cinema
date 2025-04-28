package com.example.reservation.dto.response;

import com.example.db.entity.Payment;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReservationServiceResponse {

    private String orderId;
    private String orderName;
    Long totalAmount;

    public static ReservationServiceResponse from(Payment payment) {
        return ReservationServiceResponse.builder()
                .orderId(payment.getOrderId())
                .orderName(payment.getOrderName())
                .totalAmount(payment.getTotalAmount())
                .build();
    }
}