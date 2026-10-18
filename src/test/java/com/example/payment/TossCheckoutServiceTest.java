package com.example.payment;

import com.example.payment.service.TossCheckoutService;
import com.example.payment.service.TossGateway;
import com.example.reservation.service.BookingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class TossCheckoutServiceTest {
    private final BookingService bookings = mock(BookingService.class);
    private final TossGateway gateway = mock(TossGateway.class);
    private final TossCheckoutService service = new TossCheckoutService(bookings, gateway, "toss");

    @Nested
    @DisplayName("토스 테스트 결제")
    class Confirm {
        @Test
        @DisplayName("서버 주문 금액과 다른 승인 요청은 결제사에 전달하지 않는다")
        void fail_amount_mismatch() {
            // given
            when(bookings.order("order-1", 7L)).thenReturn(new BookingService.OrderView(
                    "order-1", "영화 1매", 10000, "READY", LocalDateTime.now().plusMinutes(2),
                    LocalDateTime.now().plusDays(1), List.of("A1")));

            // when & then
            assertThatThrownBy(() -> service.confirm("order-1", 7L, "key-1", 1L))
                    .isInstanceOf(IllegalArgumentException.class);
            verifyNoInteractions(gateway);
        }

        @Test
        @DisplayName("승인 성공 후 주문을 완료한다")
        void success() {
            // given
            when(bookings.order("order-1", 7L)).thenReturn(new BookingService.OrderView(
                    "order-1", "영화 1매", 10000, "READY", LocalDateTime.now().plusMinutes(2),
                    LocalDateTime.now().plusDays(1), List.of("A1")));
            when(gateway.confirm("key-1", "order-1", 10000L))
                    .thenReturn(new TossGateway.Result("key-1", "order-1", 10000L, "DONE"));

            // when
            service.confirm("order-1", 7L, "key-1", 10000L);

            // then
            verify(bookings).startToss("order-1", 7L, "key-1");
            verify(bookings).finishToss("order-1", 7L, "key-1");
        }

        @Test
        @DisplayName("외부 승인 후 주문 완료가 실패하면 승인을 취소한다")
        void fail_record_compensates() {
            // given
            when(bookings.order("order-1", 7L)).thenReturn(new BookingService.OrderView(
                    "order-1", "영화 1매", 10000, "READY", LocalDateTime.now().plusMinutes(2),
                    LocalDateTime.now().plusDays(1), List.of("A1")));
            when(gateway.confirm("key-1", "order-1", 10000L))
                    .thenReturn(new TossGateway.Result("key-1", "order-1", 10000L, "DONE"));
            when(bookings.finishToss("order-1", 7L, "key-1"))
                    .thenThrow(new IllegalStateException("DB failure"));

            // when & then
            assertThatThrownBy(() -> service.confirm("order-1", 7L, "key-1", 10000L))
                    .isInstanceOf(IllegalStateException.class);
            verify(gateway).cancel("key-1", "order-1");
            verify(bookings).failToss("order-1", 7L);
        }

        @Test
        @DisplayName("결제사가 승인을 거절하면 주문을 다시 결제 대기로 돌린다")
        void fail_provider_retries() {
            // given
            when(bookings.order("order-1", 7L)).thenReturn(new BookingService.OrderView(
                    "order-1", "영화 1매", 10000, "READY", LocalDateTime.now().plusMinutes(2),
                    LocalDateTime.now().plusDays(1), List.of("A1")));
            when(gateway.confirm("key-1", "order-1", 10000L))
                    .thenThrow(new org.springframework.web.client.HttpClientErrorException(
                            org.springframework.http.HttpStatus.BAD_REQUEST));

            // when & then
            assertThatThrownBy(() -> service.confirm("order-1", 7L, "key-1", 10000L))
                    .isInstanceOf(org.springframework.web.client.HttpClientErrorException.class);
            verify(bookings).resetToss("order-1", 7L);
        }
    }

    @Nested
    @DisplayName("토스 결제 취소")
    class Cancel {
        @Test
        @DisplayName("결제사 취소가 실패하면 주문 좌석을 해제하지 않는다")
        void fail_gateway_keeps_booking() {
            // given
            when(bookings.order("order-1", 7L)).thenReturn(new BookingService.OrderView(
                    "order-1", "영화 1매", 10000, "DONE", LocalDateTime.now().plusMinutes(2),
                    LocalDateTime.now().plusDays(1), List.of("A1")));
            when(bookings.paymentKey("order-1", 7L)).thenReturn("key-1");
            doThrow(new IllegalStateException("refund failed")).when(gateway).cancel("key-1", "order-1");

            // when & then
            assertThatThrownBy(() -> service.cancel("order-1", 7L))
                    .isInstanceOf(IllegalStateException.class);
            verify(bookings).beginTossRefund("order-1", 7L);
            verify(bookings, never()).cancel("order-1", 7L);
        }
    }
}
