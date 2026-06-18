package com.example.payment;

import com.example.config.IntegrationServiceTest;
import com.example.db.entity.Payment;
import com.example.db.enums.PaymentMethod;
import com.example.db.enums.PaymentStatus;
import com.example.db.enums.PaymentType;
import com.example.payment.dto.request.TossPaymentConfirmServiceRequest;
import com.example.payment.exception.PaymentErrorCode;
import com.example.payment.exception.PaymentException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@Transactional
public class PaymentValidatorTest extends IntegrationServiceTest {

    @Nested
    @DisplayName("결제 승인 요청 검증")
    class Validate {

        @Test
        @DisplayName("결제 내역과 금액이 정상적으로 일치하면 예외 없이 통과")
        void success() {
            // given
            savePayment("test-key", "order-123", 10000L);

            TossPaymentConfirmServiceRequest request = TossPaymentConfirmServiceRequest.builder()
                    .paymentKey("test-key")
                    .orderId("order-123")
                    .amount(10000L)
                    .build();

            // when & then
            assertThatCode(() -> paymentValidator.validate(request))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("결제 내역이 없으면 예외 발생")
        void fail_order_not_found() {
            // given
            TossPaymentConfirmServiceRequest request = TossPaymentConfirmServiceRequest.builder()
                    .paymentKey("test-key")
                    .orderId("nonexistent-order-id")
                    .amount(10000L)
                    .build();

            // when & then
            assertThatThrownBy(() -> paymentValidator.validate(request))
                    .isInstanceOf(PaymentException.class)
                    .hasMessageContaining(PaymentErrorCode.ORDER_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("금액이 일치하지 않으면 예외 발생")
        void fail_amount_mismatch() {
            // given
            savePayment("test-key", "order-123", 10000L);

            TossPaymentConfirmServiceRequest request = TossPaymentConfirmServiceRequest.builder()
                    .paymentKey("test-key")
                    .orderId("order-123")
                    .amount(20000L)
                    .build();

            // when & then
            assertThatThrownBy(() -> paymentValidator.validate(request))
                    .isInstanceOf(PaymentException.class)
                    .hasMessageContaining(PaymentErrorCode.AMOUNT_MISMATCH.getMessage());
        }
    }

    private Payment savePayment(String paymentKey, String orderId, Long amount) {
        return paymentRepository.save(Payment.of(
                paymentKey,
                PaymentType.NORMAL,
                orderId,
                "movie ticket",
                amount,
                PaymentStatus.READY,
                PaymentMethod.CARD,
                LocalDateTime.now(),
                LocalDateTime.now(),
                null
        ));
    }
}
