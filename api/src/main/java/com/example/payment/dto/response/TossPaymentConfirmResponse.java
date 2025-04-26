package com.example.payment.dto.response;

import com.example.payment.dto.PaymentStatus;
import lombok.Getter;

@Getter
public class TossPaymentConfirmResponse {

    String paymentKey;
    String OrderId;
    String orderName;
    String totalAmount;
    String provider;
    String paymentMethod;
    PaymentStatus status;
}
