package com.example.reservation.dto.response;

import com.example.db.entity.Payment;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class ReservationCreateServiceResponse {

    private String orderId;
    private String orderName;
    Long totalAmount;
    private LocalDateTime expiresAt;

    public static ReservationCreateServiceResponse from(Payment payment) {
        return ReservationCreateServiceResponse.builder()
                .orderId(payment.getOrderId())
                .orderName(payment.getOrderName())
                .totalAmount(payment.getTotalAmount())
                .expiresAt(payment.getExpiresAt())
                .build();
    }
}
