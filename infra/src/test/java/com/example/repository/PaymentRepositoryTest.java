package com.example.repository;

import com.example.config.IntegrationRepositoryTest;
import com.example.db.entity.Payment;
import com.example.db.enums.PaymentMethod;
import com.example.db.enums.PaymentStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Transactional
public class PaymentRepositoryTest extends IntegrationRepositoryTest {

    @Nested
    @DisplayName("findByOrderId 메서드")
    class FindByOrderId {

        @Test
        @DisplayName("주문 ID로 결제 정보를 성공적으로 조회한다")
        void success() {
            // given
            savePayment("ORDER123");

            // when
            Optional<Payment> result = paymentRepository.findByOrderId("ORDER123");

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getOrderId()).isEqualTo("ORDER123");
        }
    }


    private Payment savePayment(String orderId) {
        return paymentRepository.save(Payment.builder()
                .orderId(orderId)
                .totalAmount(10000L)
                .status(PaymentStatus.DONE)
                .method(PaymentMethod.CARD)
                .build());
    }
}
