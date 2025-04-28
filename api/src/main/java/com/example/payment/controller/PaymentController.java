package com.example.payment.controller;

import com.example.payment.dto.request.TossPaymentConfirmRequest;
import com.example.payment.dto.response.TossPaymentConfirmResponse;
import com.example.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentClient paymentClient;

    @PostMapping("/toss/confirm")
    public String confirm(@RequestBody TossPaymentConfirmRequest confirmRequest){
        TossPaymentConfirmResponse response = paymentClient.confirmPayment(confirmRequest);
        paymentService.completePayment(response.toServiceRequest());
        return "reservation";
    }

    @GetMapping("/success")
    public String successPage() {
        return "success";
    }

    @GetMapping("/fail")
    public String failPage() {
        return "fail";
    }
}
