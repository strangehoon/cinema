package com.example.payment.dto.request;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CompletePaymentServiceRequest {

    String paymentKey;
    String type;
    String orderId;
    String orderName;
    String method;
    String totalAmount;
    String status;
    String requestedAt;
    String approvedAt;
}
