package com.example.payment;

import com.example.config.IntegrationServiceTest;
import com.example.db.entity.*;
import com.example.db.enums.*;
import com.example.payment.dto.request.PaymentCompleteServiceRequest;
import com.example.payment.exception.PaymentException;
import com.example.reservation.exception.ReservationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import static com.example.payment.exception.PaymentErrorCode.PAYMENT_NOT_FOUND;
import static com.example.reservation.exception.ReservationErrorCode.RESERVATION_NOT_FOUND;
import static com.example.reservation.exception.ReservationErrorCode.RESERVATION_NOT_FOUND_BY_PAYMENT;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@Transactional
public class PaymentServiceTest extends IntegrationServiceTest {

    @MockitoBean
    private ApplicationEventPublisher applicationEventPublisher;

    @Nested
    @DisplayName("completePayment 메서드")
    class CompletePayment {
        @Test
        @DisplayName("정상적인 결제 완료 요청이 들어오면 payment와 reservation이 갱신된다.")
        void success() {
            // given
            Theater theater = saveTheater();
            Movie movie = saveMovie();
            Screening screening = saveScreening(movie, theater);
            ScreeningSeat seat = saveScreeningSeat(theater);
            Payment payment = savePayment(null, null, "1",null, PaymentStatus.READY,
                    "2024-01-01T09:00:00", "2024-01-01T09:05:00");
            Reservation reservation = saveReservation(screening, seat, payment, ReservationStatus.IN_PROGRESS);

            PaymentCompleteServiceRequest request = PaymentCompleteServiceRequest.builder()
                    .paymentKey("pk-123")
                    .type("NORMAL")
                    .orderId("1")
                    .method("CARD")
                    .status("DONE")
                    .requestedAt("2024-01-01T10:00:00+09:00")
                    .approvedAt("2024-01-01T10:05:00+09:00")
                    .build();

            // when
            paymentService.completePayment(request);
            Payment updatedPayment = paymentRepository.findById(payment.getId())
                    .orElseThrow(() -> new PaymentException(PAYMENT_NOT_FOUND));
            Reservation updatedReservation = reservationRepository.findById(reservation.getId())
                    .orElseThrow(() -> new ReservationException(RESERVATION_NOT_FOUND));

            // then
            assertThat(updatedPayment.getPaymentKey()).isEqualTo("pk-123");
            assertThat(updatedPayment.getType()).isEqualTo(PaymentType.NORMAL);
            assertThat(updatedPayment.getMethod()).isEqualTo(PaymentMethod.CARD);
            assertThat(updatedPayment.getStatus()).isEqualTo(PaymentStatus.DONE);
            assertThat(updatedPayment.getRequestedAt()).isEqualTo(LocalDateTime.of(2024, 1, 1, 10, 0));
            assertThat(updatedPayment.getApprovedAt()).isEqualTo(LocalDateTime.of(2024, 1, 1, 10, 5));
            assertThat(updatedReservation.getStatus()).isEqualTo(ReservationStatus.COMPLETED);
        }

        @Test
        @DisplayName("존재하지 않는 orderId로 요청하면 PaymentException이 발생한다.")
        void fail_payment_not_found() {
            // given
            PaymentCompleteServiceRequest request = PaymentCompleteServiceRequest.builder()
                    .orderId("non-existent")
                    .build();

            // expect
            assertThatThrownBy(() -> paymentService.completePayment(request))
                    .isInstanceOf(PaymentException.class)
                    .hasMessage(PAYMENT_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("결제는 존재하지만 연결된 예약이 없으면 예외가 발생한다.")
        void fail_reservation_not_found() {
            // given
            Payment payment = savePayment(null, null, "1", null, PaymentStatus.READY,
                    "2024-01-01T09:00:00", "2024-01-01T09:05:00");

            PaymentCompleteServiceRequest request = PaymentCompleteServiceRequest.builder()
                    .paymentKey("pk-123")
                    .type("NORMAL")
                    .orderId("1")
                    .method("CARD")
                    .status("DONE")
                    .requestedAt("2024-01-01T10:00:00+09:00")
                    .approvedAt("2024-01-01T10:05:00+09:00")
                    .build();

            // expect
            assertThatThrownBy(() -> paymentService.completePayment(request))
                    .isInstanceOf(ReservationException.class)
                    .hasMessage(RESERVATION_NOT_FOUND_BY_PAYMENT.getMessage());
        }
    }

    private Movie saveMovie() {
        return movieRepository.save(Movie.builder()
                .title("movie1")
                .rating(Rating.R_12)
                .releasedDate(LocalDate.of(2024, 1, 1))
                .thumbnailImage("movie1-thumbnail.jpg")
                .runningTimeMin(120)
                .genre(Genre.DRAMA)
                .build());
    }

    private Theater saveTheater() {
        return theaterRepository.save(Theater.builder()
                .name("theater1")
                .build());
    }

    private Screening saveScreening(Movie movie, Theater theater) {
        return screeningRepository.save(Screening.builder()
                .date(LocalDate.of(2024, 1, 1))
                .startedAt(LocalDateTime.of(2024, 1, 1, 14, 0))
                .endedAt(LocalDateTime.of(2024, 1, 1, 16, 0))
                .movie(movie)
                .theater(theater)
                .build());
    }

    private ScreeningSeat saveScreeningSeat(Theater theater) {
        return screeningSeatRepository.save(ScreeningSeat.builder()
                .row(1)
                .col(1)
                .theater(theater)
                .build());
    }

    private Payment savePayment(String paymentKey, PaymentType type, String orderId, PaymentMethod method, PaymentStatus status,
                                String requestedAt, String approvedAt) {
        return paymentRepository.save(Payment.of(
                paymentKey,
                type,
                orderId,
                "oreder1",
                1L,
                status,
                method,
                LocalDateTime.parse(requestedAt),
                LocalDateTime.parse(approvedAt),
                null
        ));
    }

    private Reservation saveReservation(Screening screening, ScreeningSeat seat, Payment payment, ReservationStatus status) {
        return reservationRepository.save(Reservation.builder()
                .screening(screening)
                .screeningSeat(seat)
                .payment(payment)
                .status(status)
                .build());
    }
}
