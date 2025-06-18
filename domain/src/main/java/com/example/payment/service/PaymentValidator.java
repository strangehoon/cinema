package com.example.payment.service;

import com.example.db.entity.Payment;
import com.example.db.repository.PaymentRepository;
import com.example.payment.dto.request.TossPaymentConfirmServiceRequest;
import com.example.payment.exception.PaymentException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import static com.example.payment.exception.PaymentErrorCode.AMOUNT_MISMATCH;
import static com.example.payment.exception.PaymentErrorCode.ORDER_NOT_FOUND;

@Component
@RequiredArgsConstructor
public class PaymentValidator {

    private final PaymentRepository paymentRepository;

    public void validate(TossPaymentConfirmServiceRequest request){

        Payment payment = paymentRepository.findByOrderId(request.getOrderId())
                .orElseThrow(() -> new PaymentException(ORDER_NOT_FOUND));

        if (!payment.getTotalAmount().equals(request.getAmount())) {
            throw new PaymentException(AMOUNT_MISMATCH);
        }
    }
}