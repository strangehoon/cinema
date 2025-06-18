package com.example.repository;

import com.example.config.IntegrationRepositoryTest;
import com.example.db.entity.*;
import com.example.db.enums.Genre;
import com.example.db.enums.Rating;
import com.example.db.enums.PaymentMethod;
import com.example.db.enums.PaymentStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class ReservationRepositoryTest extends IntegrationRepositoryTest {

    @Nested
    @DisplayName("countByUserIdAndScreeningId 메서드")
    class CountByUserIdAndScreeningId {

        @Test
        @DisplayName("성공적으로 특정 사용자의 상영 회차 예약 횟수를 조회한다")
        void success() {
            // given
            User user = saveUser();
            Movie movie = saveMovie();
            Theater theater = saveTheater();
            Screening screening = saveScreening(movie, theater);
            ScreeningSeat screeningSeat = saveScreeningSeat(theater);
            saveReservation(screeningSeat, screening, user);

            // when
            int count = reservationRepository.countByUserIdAndScreeningId(user.getId(), screening.getId());

            // then
            assertThat(count).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("findByScreeningIdAndScreeningSeatId 메서드")
    class FindByScreeningIdAndScreeningSeatId {

        @Test
        @DisplayName("screeningId와 seatId로 예약 정보를 조회한다")
        void success() {
            // given
            Movie movie = saveMovie();
            Theater theater = saveTheater();
            Screening screening = saveScreening(movie, theater);
            ScreeningSeat screeningSeat = saveScreeningSeat(theater);
            Reservation reservation = saveReservation(screeningSeat, screening, null);

            // when
            List<Reservation> reservations = reservationRepository.findByScreeningIdAndScreeningSeatId(
                    screening.getId(),
                    List.of(screeningSeat.getId())
            );

            // then
            assertThat(reservations).hasSize(1);
            assertThat(reservations.get(0).getId()).isEqualTo(reservation.getId());
        }
    }

    @Nested
    @DisplayName("findByPaymentId 메서드")
    class FindByPaymentId {

        @Test
        @DisplayName("paymentId로 예약 정보를 조회한다")
        void success() {
            // given
            Movie movie = saveMovie();
            Theater theater = saveTheater();
            Screening screening = saveScreening(movie, theater);
            ScreeningSeat screeningSeat = saveScreeningSeat(theater);
            Payment payment = savePayment();
            Reservation reservation = saveReservationWithPayment(screeningSeat, screening, null, payment);

            // when
            List<Reservation> reservations = reservationRepository.findByPaymentId(payment.getId());

            // then
            assertThat(reservations).hasSize(1);
            assertThat(reservations.get(0).getPayment().getId()).isEqualTo(payment.getId());
        }
    }

    private User saveUser() {
        return userRepository.save(User.builder()
                .name("user1")
                .build());
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

    private Reservation saveReservation(ScreeningSeat screeningSeat, Screening screening, User user) {
        return reservationRepository.save(Reservation.builder()
                .screeningSeat(screeningSeat)
                .screening(screening)
                .user(user)
                .build());
    }

    private Reservation saveReservationWithPayment(ScreeningSeat screeningSeat, Screening screening, User user, Payment payment) {
        return reservationRepository.save(Reservation.builder()
                .screeningSeat(screeningSeat)
                .screening(screening)
                .user(user)
                .payment(payment)
                .build());
    }

    private Payment savePayment() {
        return paymentRepository.save(Payment.builder()
                .orderId("ORDER123")
                .totalAmount(10000L)
                .status(PaymentStatus.DONE)
                .method(PaymentMethod.CARD)
                .build());
    }
}