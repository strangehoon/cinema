package com.example.payment.controller;

import com.example.common.ApiResponse;
import com.example.payment.dto.request.TossPaymentConfirmRequest;
import com.example.payment.dto.response.TossPaymentConfirmResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/toss")
public class PaymentController {

    private final PaymentClient paymentClient;

    @PostMapping("/confirm")
    public TossPaymentConfirmResponse confirm(@RequestBody TossPaymentConfirmRequest confirmRequest){
        return paymentClient.confirmPayment(confirmRequest);
    }
}
