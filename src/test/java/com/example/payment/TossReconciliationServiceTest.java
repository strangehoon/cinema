package com.example.payment;

import com.example.payment.service.TossGateway;
import com.example.payment.service.TossReconciliationService;
import com.example.reservation.service.BookingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.*;

class TossReconciliationServiceTest {
    private final BookingService bookings = mock(BookingService.class);
    private final TossGateway gateway = mock(TossGateway.class);
    private final TossReconciliationService service = new TossReconciliationService(bookings, gateway, "toss");

    @Nested
    @DisplayName("미확정 토스 결제 복구")
    class Reconcile {
        @Test
        @DisplayName("결제사에 완료된 주문은 좌석을 유지하고 완료 처리한다")
        void success_paid() {
            // given
            when(bookings.pendingToss()).thenReturn(List.of(new BookingService.PendingToss(
                    "order-1", 7L, "key-1", 10000L)));
            when(gateway.lookup("order-1"))
                    .thenReturn(new TossGateway.Result("key-1", "order-1", 10000L, "DONE"));

            // when
            service.reconcile();

            // then
            verify(bookings).finishToss("order-1", 7L, "key-1");
            verify(bookings, never()).failToss(anyString(), anyLong());
        }

        @Test
        @DisplayName("결제사 응답이 없으면 좌석을 해제하지 않는다")
        void success_unknown_keeps_hold() {
            // given
            when(bookings.pendingToss()).thenReturn(List.of(new BookingService.PendingToss(
                    "order-1", 7L, "key-1", 10000L)));
            when(gateway.lookup("order-1")).thenReturn(null);

            // when
            service.reconcile();

            // then
            verify(bookings, never()).failToss(anyString(), anyLong());
            verify(bookings, never()).finishToss(anyString(), anyLong(), anyString());
        }

        @Test
        @DisplayName("결제사가 취소를 확정한 주문만 좌석을 해제한다")
        void success_canceled_releases() {
            // given
            when(bookings.pendingToss()).thenReturn(List.of(new BookingService.PendingToss(
                    "order-1", 7L, "key-1", 10000L)));
            when(gateway.lookup("order-1"))
                    .thenReturn(new TossGateway.Result("key-1", "order-1", 10000L, "CANCELED"));

            // when
            service.reconcile();

            // then
            verify(bookings).failToss("order-1", 7L);
        }

        @Test
        @DisplayName("환불 중인 주문은 결제사 취소가 확인된 뒤에만 해제한다")
        void success_refund_recovery() {
            // given
            when(bookings.pendingRefunds()).thenReturn(List.of(new BookingService.PendingToss(
                    "order-2", 7L, "key-2", 10000L)));
            when(gateway.lookup("order-2"))
                    .thenReturn(new TossGateway.Result("key-2", "order-2", 10000L, "CANCELED"));

            // when
            service.reconcile();

            // then
            verify(bookings).cancel("order-2", 7L);
        }
    }
}
