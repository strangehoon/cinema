package com.example.payment;

import com.example.config.IntegrationControllerSupport;
import com.example.payment.dto.request.TossPaymentConfirmRequest;
import com.example.payment.dto.response.TossPaymentConfirmResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

public class PaymentControllerTest extends IntegrationControllerSupport {

    @Nested
    @DisplayName("토스 결제 승인")
    class ConfirmPayment {

        @Test
        @DisplayName("정상적인 결제 승인 요청이 들어오면 예약 페이지로 이동한다")
        void success() throws Exception {
            // given
            TossPaymentConfirmRequest request = TossPaymentConfirmRequest.builder()
                    .paymentKey("test-payment-key")
                    .orderId("ORDER1234")
                    .amount(24000L)
                    .build();

            TossPaymentConfirmResponse response = TossPaymentConfirmResponse.builder()
                    .orderId("ORDER1234")
                    .paymentKey("test-payment-key")
                    .totalAmount("24000")
                    .status("DONE")
                    .build();

            given(paymentClient.confirmPayment(any())).willReturn(response);
            doNothing().when(paymentService).completePayment(any());

            // when & then
            mockMvc.perform(post("/toss/confirm")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(view().name("reservation"));
        }
    }
}
