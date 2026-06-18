package com.example.payment.dto.request;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TossPaymentConfirmRequest {
    private String paymentKey;
    private String orderId;
    private Long amount;

    public TossPaymentConfirmServiceRequest toServiceRequest() {
        return TossPaymentConfirmServiceRequest.builder()
                .paymentKey(paymentKey)
                .orderId(orderId)
                .amount(amount)
                .build();
    }
}
