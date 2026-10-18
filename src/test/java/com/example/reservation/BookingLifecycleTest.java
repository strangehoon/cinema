package com.example.reservation;

import com.example.config.IntegrationServiceTest;
import com.example.db.entity.*;
import com.example.db.enums.*;
import com.example.reservation.dto.request.ReservationCreateServiceRequest;
import com.example.reservation.service.BookingService;
import com.example.db.repository.BookingSeatRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BookingLifecycleTest extends IntegrationServiceTest {
    @Autowired BookingService bookings;
    @Autowired BookingSeatRepository bookingSeatRepository;
    @Autowired JdbcTemplate jdbc;

    @AfterEach
    void tearDown() {
        bookingSeatRepository.deleteAllInBatch();
        reservationRepository.deleteAllInBatch();
        paymentRepository.deleteAllInBatch();
        bookingOrderRepository.deleteAllInBatch();
        screeningSeatRepository.deleteAllInBatch();
        screeningRepository.deleteAllInBatch();
        movieRepository.deleteAllInBatch();
        theaterRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @Nested
    @DisplayName("모의 결제 및 예매 취소")
    class Lifecycle {
        @Test
        @DisplayName("예매, 결제, 취소 후 좌석은 다시 판매 가능하고 주문 이력은 남는다")
        void success() {
            // given
            User user = userRepository.save(User.builder().name("관객").build());
            Theater theater = theaterRepository.save(Theater.builder().name("1관").build());
            Movie movie = movieRepository.save(Movie.builder().title("영화").genre(Genre.DRAMA)
                    .rating(Rating.R_ALL).releasedDate(LocalDate.now()).runningTimeMin(100).build());
            Screening screening = screeningRepository.save(Screening.builder().movie(movie).theater(theater)
                    .date(LocalDate.now().plusDays(1)).startedAt(LocalDateTime.now().plusDays(1))
                    .endedAt(LocalDateTime.now().plusDays(1).plusHours(2)).build());
            ScreeningSeat seat = screeningSeatRepository.save(ScreeningSeat.builder().theater(theater).row(1).col(1).build());
            Reservation inventory = reservationRepository.save(Reservation.builder().screening(screening)
                    .screeningSeat(seat).price(12000).status(ReservationStatus.NONE).build());
            String orderId = reservationService.createReserve(ReservationCreateServiceRequest.builder()
                    .userId(user.getId()).screeningId(screening.getId()).seatIds(List.of(seat.getId())).build()).getOrderId();

            // then
            assertThat(jdbc.queryForObject("select count(*) from booking_orders where order_id = ? and total_amount = ?",
                    Integer.class, orderId, 12000L)).isEqualTo(1);
            assertThat(jdbc.queryForObject("select count(*) from booking_seats s join booking_orders o " +
                    "on s.booking_order_id = o.id where o.order_id = ?", Integer.class, orderId)).isEqualTo(1);

            // when
            bookings.completeMock(orderId, user.getId());
            bookings.cancel(orderId, user.getId());

            // then
            assertThat(reservationRepository.findById(inventory.getId()).orElseThrow().getStatus()).isEqualTo(ReservationStatus.NONE);
            assertThat(bookings.orders(user.getId())).singleElement().satisfies(order -> {
                assertThat(order.status()).isEqualTo("CANCELED");
                assertThat(order.seats()).containsExactly("A1");
            });
        }

        @Test
        @DisplayName("다른 사용자의 결제는 완료할 수 없다")
        void fail_other_user() {
            // given
            User owner = userRepository.save(User.builder().name("소유자").build());
            User other = userRepository.save(User.builder().name("타인").build());
            Payment payment = paymentRepository.save(Payment.of("order-1", "영화 1매", 12000L, PaymentStatus.READY, owner));

            // when & then
            assertThatThrownBy(() -> bookings.completeMock(payment.getOrderId(), other.getId()))
                    .isInstanceOf(SecurityException.class);
        }

        @Test
        @DisplayName("결제 대기 시간이 지나면 좌석을 해제하고 주문 이력은 만료로 남긴다")
        void success_expire() {
            // given
            User user = userRepository.save(User.builder().name("관객").build());
            Theater theater = theaterRepository.save(Theater.builder().name("1관").build());
            Movie movie = movieRepository.save(Movie.builder().title("영화").genre(Genre.DRAMA)
                    .rating(Rating.R_ALL).releasedDate(LocalDate.now()).runningTimeMin(100).build());
            Screening screening = screeningRepository.save(Screening.builder().movie(movie).theater(theater)
                    .date(LocalDate.now().plusDays(1)).startedAt(LocalDateTime.now().plusDays(1))
                    .endedAt(LocalDateTime.now().plusDays(1).plusHours(2)).build());
            ScreeningSeat seat = screeningSeatRepository.save(ScreeningSeat.builder().theater(theater).row(1).col(1).build());
            reservationRepository.save(Reservation.builder().screening(screening).screeningSeat(seat)
                    .price(12000).status(ReservationStatus.NONE).build());
            String orderId = reservationService.createReserve(ReservationCreateServiceRequest.builder()
                    .userId(user.getId()).screeningId(screening.getId()).seatIds(List.of(seat.getId())).build()).getOrderId();
            Payment payment = paymentRepository.findByOrderId(orderId).orElseThrow();
            payment.setExpiresAt(LocalDateTime.now().minusSeconds(1));
            paymentRepository.save(payment);

            // when
            bookings.expireDue();

            // then
            assertThat(paymentRepository.findByOrderId(orderId).orElseThrow().getStatus()).isEqualTo(PaymentStatus.EXPIRED);
            assertThat(reservationRepository.findByScreeningId(screening.getId()))
                    .allMatch(r -> r.getStatus() == ReservationStatus.NONE);
            assertThat(bookings.orders(user.getId())).singleElement().satisfies(order ->
                    assertThat(order.seats()).containsExactly("A1"));
        }

        @Test
        @DisplayName("결제 전 주문도 직접 취소할 수 있다")
        void success_cancel_ready() {
            // given
            User owner = userRepository.save(User.builder().name("관객").build());
            Payment payment = paymentRepository.save(Payment.of("ready-order", "영화 1매", 10000L,
                    PaymentStatus.READY, owner));

            // when
            var canceled = bookings.cancel(payment.getOrderId(), owner.getId());

            // then
            assertThat(canceled.status()).isEqualTo("CANCELED");
        }

        @Test
        @DisplayName("모의 결제 실패 뒤에도 보유 시간 안에는 다시 결제할 수 있다")
        void fail_mock_payment_retry() {
            // given
            User owner = userRepository.save(User.builder().name("관객").build());
            Payment payment = paymentRepository.save(Payment.of("retry-order", "영화 1매", 10000L,
                    PaymentStatus.READY, owner));
            payment.setExpiresAt(LocalDateTime.now().plusMinutes(2));
            paymentRepository.save(payment);

            // when & then
            assertThatThrownBy(() -> bookings.failMock(payment.getOrderId(), owner.getId()))
                    .isInstanceOf(IllegalStateException.class);
            assertThat(bookings.order(payment.getOrderId(), owner.getId()).status()).isEqualTo("READY");
            assertThat(bookings.completeMock(payment.getOrderId(), owner.getId()).status()).isEqualTo("DONE");
        }

        @Test
        @DisplayName("토스 승인 확인 중인 주문은 보유 시간이 지나도 해제하지 않는다")
        void success_toss_pending_survives_expiry() {
            // given
            User owner = userRepository.save(User.builder().name("관객").build());
            Payment payment = paymentRepository.save(Payment.of("pending-order", "영화 1매", 10000L,
                    PaymentStatus.READY, owner));
            payment.setExpiresAt(LocalDateTime.now().plusMinutes(2));
            paymentRepository.save(payment);
            bookings.startToss(payment.getOrderId(), owner.getId(), "key-1");
            Payment pending = paymentRepository.findByOrderId(payment.getOrderId()).orElseThrow();
            pending.setExpiresAt(LocalDateTime.now().minusSeconds(1));
            paymentRepository.save(pending);

            // when
            bookings.expireDue();

            // then
            assertThat(bookings.order(payment.getOrderId(), owner.getId()).status()).isEqualTo("IN_PROGRESS");
            assertThat(bookings.finishToss(payment.getOrderId(), owner.getId(), "key-1").status())
                    .isEqualTo("DONE");
        }

        @Test
        @DisplayName("결제사 거절 뒤 보유 시간이 남으면 다시 결제할 수 있다")
        void success_toss_decline_retry() {
            // given
            User owner = userRepository.save(User.builder().name("관객").build());
            Payment payment = paymentRepository.save(Payment.of("declined-order", "영화 1매", 10000L,
                    PaymentStatus.READY, owner));
            payment.setExpiresAt(LocalDateTime.now().plusMinutes(2));
            paymentRepository.save(payment);
            bookings.startToss(payment.getOrderId(), owner.getId(), "key-1");

            // when
            bookings.resetToss(payment.getOrderId(), owner.getId());

            // then
            assertThat(bookings.order(payment.getOrderId(), owner.getId()).status()).isEqualTo("READY");
            assertThat(bookings.paymentKey(payment.getOrderId(), owner.getId())).isNull();
            assertThat(bookings.startToss(payment.getOrderId(), owner.getId(), "key-2").status())
                    .isEqualTo("IN_PROGRESS");
        }

        @Test
        @DisplayName("환불 확인 중인 주문은 취소 확정 전까지 결제 완료 상태의 좌석을 유지한다")
        void success_refund_pending() {
            // given
            User owner = userRepository.save(User.builder().name("관객").build());
            Payment payment = paymentRepository.save(Payment.of("refund-order", "영화 1매", 10000L,
                    PaymentStatus.DONE, owner));
            payment.setPaymentKey("key-1");
            paymentRepository.save(payment);

            // when
            var pending = bookings.beginTossRefund(payment.getOrderId(), owner.getId());

            // then
            assertThat(pending.status()).isEqualTo("REFUNDING");
            assertThat(bookings.pendingRefunds()).singleElement()
                    .satisfies(refund -> assertThat(refund.paymentKey()).isEqualTo("key-1"));
            assertThat(bookings.cancel(payment.getOrderId(), owner.getId()).status()).isEqualTo("CANCELED");
        }
    }
}
