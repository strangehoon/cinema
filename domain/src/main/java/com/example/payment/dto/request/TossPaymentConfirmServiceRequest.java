package com.example.payment.dto.request;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TossPaymentConfirmServiceRequest {
    private String paymentKey;
    private String orderId;
    private Long amount;
}
