package com.example.reservation;

import com.example.db.entity.Payment;
import com.example.db.entity.Reservation;
import com.example.db.entity.User;
import com.example.db.enums.PaymentStatus;
import com.example.db.repository.BookingSeatRepository;
import com.example.db.repository.PaymentRepository;
import com.example.db.repository.ReservationRepository;
import com.example.reservation.event.ReservationCompletedEvent;
import com.example.reservation.service.BookingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.time.Clock;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class BookingServiceTest {
    private final PaymentRepository payments = mock(PaymentRepository.class);
    private final ReservationRepository reservations = mock(ReservationRepository.class);
    private final BookingSeatRepository bookingSeats = mock(BookingSeatRepository.class);
    private final ApplicationEventPublisher events = mock(ApplicationEventPublisher.class);
    private final Clock clock = Clock.fixed(java.time.Instant.parse("2026-10-21T00:00:00Z"), ZoneId.of("Asia/Seoul"));
    private final BookingService bookings = new BookingService(payments, reservations, bookingSeats, events, clock);

    @Nested
    @DisplayName("예매 완료 알림")
    class Notification {
        @Test
        @DisplayName("실제 사용자 이름과 예약 좌석 수를 완료 이벤트에 담는다")
        void success() {
            // given
            User user = User.builder().name("관객").build();
            org.springframework.test.util.ReflectionTestUtils.setField(user, "id", 7L);
            Payment payment = Payment.of("order-1", "영화 2매", 20000L, PaymentStatus.READY, user);
            payment.setExpiresAt(LocalDateTime.now(clock).plusMinutes(2));
            when(payments.findByOrderId("order-1")).thenReturn(Optional.of(payment));
            when(reservations.findByPaymentId(payment.getId()))
                    .thenReturn(List.of(mock(Reservation.class), mock(Reservation.class)));

            // when
            bookings.completeMock("order-1", 7L);

            // then
            var captor = org.mockito.ArgumentCaptor.forClass(ReservationCompletedEvent.class);
            verify(events).publishEvent(captor.capture());
            assertThat(captor.getValue().getUserName()).isEqualTo("관객");
            assertThat(captor.getValue().getSeatCount()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("결제 보유 시간")
    class HoldDeadline {
        @Test
        @DisplayName("만료 시각과 같은 순간에는 결제할 수 없다")
        void fail_at_expiry_boundary() {
            // given
            User user = User.builder().name("관객").build();
            org.springframework.test.util.ReflectionTestUtils.setField(user, "id", 7L);
            Payment payment = Payment.of("order-1", "영화 1매", 10000L, PaymentStatus.READY, user);
            payment.setExpiresAt(LocalDateTime.now(clock));
            when(payments.findByOrderId("order-1")).thenReturn(Optional.of(payment));

            // when & then
            assertThatThrownBy(() -> bookings.completeMock("order-1", 7L))
                    .isInstanceOf(IllegalArgumentException.class);
            verifyNoInteractions(events);
        }
    }
}
