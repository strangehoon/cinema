package com.example.payment.service;

import com.example.db.entity.Payment;
import com.example.db.repository.PaymentRepository;
import com.example.payment.dto.request.TossPaymentConfirmServiceRequest;
import com.example.payment.exception.PaymentConfirmException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import static com.example.payment.exception.PaymentConfirmErrorCode.AMOUNT_MISMATCH;

@Component
@RequiredArgsConstructor
public class PaymentValidator {

    private final PaymentRepository paymentRepository;

    public void validate(TossPaymentConfirmServiceRequest request){

        Payment payment = paymentRepository.findByOrderId(request.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다. orderId=" + request.getOrderId()));

        if (!payment.getTotalAmount().equals(request.getAmount())) {
            throw new PaymentConfirmException(AMOUNT_MISMATCH);
        }
    }
}