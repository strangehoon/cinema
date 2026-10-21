package com.example.db.entity;

import com.example.db.enums.PaymentStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BookingOrderTest {
    @Nested
    @DisplayName("결제 상태 전이")
    class StatusTransition {
        @Test
        @DisplayName("승인과 환불의 정상 상태 전이를 허용한다")
        void success_payment_and_refund() {
            // given
            BookingOrder order = new BookingOrder("order-1", "영화 1매", 10000L, PaymentStatus.READY, null);

            // when
            order.setStatus(PaymentStatus.IN_PROGRESS);
            order.setStatus(PaymentStatus.DONE);
            order.setStatus(PaymentStatus.REFUNDING);
            order.setStatus(PaymentStatus.CANCELED);

            // then
            assertThat(order.getStatus()).isEqualTo(PaymentStatus.CANCELED);
        }

        @Test
        @DisplayName("결제 대기에서 환불 중으로 건너뛰지 못한다")
        void fail_invalid_transition() {
            // given
            BookingOrder order = new BookingOrder("order-1", "영화 1매", 10000L, PaymentStatus.READY, null);

            // when & then
            assertThatThrownBy(() -> order.setStatus(PaymentStatus.REFUNDING))
                    .isInstanceOf(IllegalStateException.class);
            assertThat(order.getStatus()).isEqualTo(PaymentStatus.READY);
        }

        @Test
        @DisplayName("만료된 주문은 결제 대기로 되돌릴 수 없다")
        void fail_reopen_expired_order() {
            // given
            BookingOrder order = new BookingOrder("order-1", "영화 1매", 10000L, PaymentStatus.READY, null);
            order.setStatus(PaymentStatus.EXPIRED);

            // when & then
            assertThatThrownBy(() -> order.setStatus(PaymentStatus.READY))
                    .isInstanceOf(IllegalStateException.class);
            assertThat(order.getStatus()).isEqualTo(PaymentStatus.EXPIRED);
        }
    }
}
