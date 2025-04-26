package com.example.payment.dto.request;

import lombok.Getter;

@Getter
public class TossPaymentConfirmRequest {

    private String paymentKey;
    private String orderId;
    private Long amount;
}
