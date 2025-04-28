package com.example.payment.dto.response;

import com.example.payment.dto.request.CompletePaymentServiceRequest;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TossPaymentConfirmResponse {

    String paymentKey;
    String type;
    String orderId;
    String orderName;
    String method;
    String totalAmount;
    String status;
    String requestedAt;
    String approvedAt;

    public CompletePaymentServiceRequest toServiceRequest() {
        return CompletePaymentServiceRequest.builder()
                .paymentKey(paymentKey)
                .type(type)
                .orderId(orderId)
                .orderName(orderName)
                .method(method)
                .totalAmount(totalAmount)
                .status(status)
                .requestedAt(requestedAt)
                .approvedAt(approvedAt)
                .build();
    }
}