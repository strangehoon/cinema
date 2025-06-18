package com.example.reservation;

import com.example.config.IntegrationServiceTest;
import com.example.db.entity.*;
import com.example.db.enums.Genre;
import com.example.db.enums.PaymentStatus;
import com.example.db.enums.Rating;
import com.example.db.enums.ReservationStatus;
import com.example.reservation.dto.request.ReservationCreateServiceRequest;
import com.example.reservation.dto.response.ReservationCreateServiceResponse;
import com.example.reservation.service.ReservationValidator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;
import static org.assertj.core.api.Assertions.assertThat;

class ReservationServiceTest extends IntegrationServiceTest {

    @MockitoBean
    private ReservationValidator reservationValidator;

    // redisson 분산락의 트랜잭션 전파 옵션 때문에 @Transactional 제거
    @AfterEach
    void tearDown() {
        reservationRepository.deleteAllInBatch();
        screeningSeatRepository.deleteAllInBatch();
        screeningRepository.deleteAllInBatch();
        theaterRepository.deleteAllInBatch();
        movieRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
        paymentRepository.deleteAllInBatch();
    }

    @Nested
    @DisplayName("영화 좌석 예약")
    class ReserveSeats {

        @Test
        @DisplayName("정상적인 요청으로 좌석 예약에 성공한다")
        void success() {
            // given
            User user = saveUser();
            Theater theater = saveTheater();
            Movie movie = saveMovie();
            Screening screening = saveScreening(movie, theater);
            List<ScreeningSeat> seats = saveScreeningSeats(theater, 1);
            List<Reservation> reservations = saveReservationsForSeats(screening, seats);
            List<Long> seatIds = seats.stream()
                    .map(ScreeningSeat::getId)
                    .toList();

            ReservationCreateServiceRequest request = ReservationCreateServiceRequest.builder()
                    .userId(user.getId())
                    .screeningId(screening.getId())
                    .seatIds(seatIds)
                    .build();

            // when
            ReservationCreateServiceResponse response = reservationService.createReserve(request);
            Payment payment = paymentRepository.findByOrderId(response.getOrderId()).orElseThrow();

            // then
            assertThat(payment.getUser().getId()).isEqualTo(user.getId());
            assertThat(payment.getTotalAmount()).isEqualTo(reservations.stream().mapToLong(Reservation::getPrice).sum());
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.READY);
            assertThat(payment.getOrderName()).contains(movie.getTitle());
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

        private List<ScreeningSeat> saveScreeningSeats(Theater theater, int count) {
            return IntStream.range(0, count)
                    .mapToObj(i -> ScreeningSeat.builder()
                            .row(1)
                            .col(i + 1)
                            .theater(theater)
                            .build())
                    .map(screeningSeatRepository::save)
                    .toList();
        }

        private List<Reservation> saveReservationsForSeats(Screening screening, List<ScreeningSeat> seats) {
            return seats.stream()
                    .map(seat -> Reservation.builder()
                            .screening(screening)
                            .screeningSeat(seat)
                            .price(10000)
                            .user(null)
                            .status(ReservationStatus.NONE)
                            .build())
                    .map(reservationRepository::save)
                    .toList();
        }
    }
}