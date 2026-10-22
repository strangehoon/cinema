package com.example.reservation;

import com.example.config.IntegrationServiceTest;
import com.example.db.entity.*;
import com.example.db.enums.Genre;
import com.example.db.enums.PaymentStatus;
import com.example.db.enums.Rating;
import com.example.db.enums.ReservationStatus;
import com.example.reservation.dto.request.ReservationCreateServiceRequest;
import com.example.reservation.dto.response.ReservationCreateServiceResponse;
import com.example.reservation.service.BookingService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import com.example.reservation.exception.ReservationException;
import com.example.db.repository.BookingSeatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReservationServiceTest extends IntegrationServiceTest {

    @Autowired
    private BookingSeatRepository bookingSeatRepository;

    @Autowired
    private BookingService bookingService;

    @AfterEach
    void tearDown() {
        bookingSeatRepository.deleteAllInBatch();
        reservationRepository.deleteAllInBatch();
        paymentRepository.deleteAllInBatch();
        bookingOrderRepository.deleteAllInBatch();
        screeningSeatRepository.deleteAllInBatch();
        screeningRepository.deleteAllInBatch();
        theaterRepository.deleteAllInBatch();
        movieRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @Nested
    @DisplayName("영화 좌석 예약")
    class ReserveSeats {

        @Test
        @DisplayName("좌석 목록이 없으면 저장소 조회 전에 요청을 거절한다")
        void fail_missing_seats() {
            // given
            User user = saveUser();
            Theater theater = saveTheater();
            Screening screening = saveScreening(saveMovie(), theater);
            ReservationCreateServiceRequest request = ReservationCreateServiceRequest.builder()
                    .userId(user.getId()).screeningId(screening.getId()).seatIds(null).build();

            // when & then
            assertThatThrownBy(() -> reservationService.createReserve(request))
                    .isInstanceOf(ReservationException.class)
                    .hasMessage("유효하지 않은 좌석이 포함되어 있습니다.");
        }

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
            assertThat(reservationRepository.findByPaymentId(payment.getId()))
                    .allMatch(reservation -> reservation.getStatus() == ReservationStatus.IN_PROGRESS);
        }

        @Test
        @DisplayName("이미 시작한 회차는 좌석과 주문을 변경하지 않는다")
        void fail_started_screening() {
            // given
            User user = saveUser();
            Theater theater = saveTheater();
            Movie movie = saveMovie();
            Screening screening = screeningRepository.save(Screening.builder()
                    .date(LocalDate.of(2024, 1, 1))
                    .startedAt(LocalDateTime.of(2024, 1, 1, 14, 0))
                    .endedAt(LocalDateTime.of(2024, 1, 1, 16, 0))
                    .movie(movie).theater(theater).build());
            ScreeningSeat seat = saveScreeningSeats(theater, 1).getFirst();
            Reservation inventory = saveReservationsForSeats(screening, List.of(seat)).getFirst();
            ReservationCreateServiceRequest request = ReservationCreateServiceRequest.builder()
                    .userId(user.getId()).screeningId(screening.getId())
                    .seatIds(List.of(seat.getId())).build();

            // when & then
            assertThatThrownBy(() -> reservationService.createReserve(request))
                    .isInstanceOf(ReservationException.class)
                    .hasMessageContaining("상영");
            assertThat(reservationRepository.findById(inventory.getId()).orElseThrow().getStatus())
                    .isEqualTo(ReservationStatus.NONE);
        }

        @Test
        @DisplayName("다른 상영관 좌석은 재고 행이 있더라도 예매할 수 없다")
        void fail_other_theater_seat() {
            // given
            User user = saveUser();
            Theater screeningTheater = saveTheater();
            Theater anotherTheater = theaterRepository.save(Theater.builder().name("다른 관").build());
            Screening screening = saveScreening(saveMovie(), screeningTheater);
            ScreeningSeat seat = saveScreeningSeats(anotherTheater, 1).getFirst();
            saveReservationsForSeats(screening, List.of(seat));
            ReservationCreateServiceRequest request = ReservationCreateServiceRequest.builder()
                    .userId(user.getId()).screeningId(screening.getId()).seatIds(List.of(seat.getId())).build();

            // when & then
            assertThatThrownBy(() -> reservationService.createReserve(request))
                    .isInstanceOf(ReservationException.class)
                    .hasMessageContaining("좌석");
        }

        @Test
        @DisplayName("만료된 보유 좌석은 스케줄러가 해제한 뒤 다시 예약할 수 있다")
        void success_reuse_expired_hold_after_scheduler() {
            // given
            User first = saveUser();
            User next = userRepository.save(User.builder().name("next").build());
            Theater theater = saveTheater();
            Screening screening = saveScreening(saveMovie(), theater);
            ScreeningSeat seat = saveScreeningSeats(theater, 1).getFirst();
            saveReservationsForSeats(screening, List.of(seat));
            var firstOrder = reservationService.createReserve(ReservationCreateServiceRequest.builder()
                    .userId(first.getId()).screeningId(screening.getId())
                    .seatIds(List.of(seat.getId())).build());
            Payment expired = paymentRepository.findByOrderId(firstOrder.getOrderId()).orElseThrow();
            expired.setExpiresAt(LocalDateTime.now(clock).minusSeconds(1));
            paymentRepository.save(expired);

            // when & then
            var nextRequest = ReservationCreateServiceRequest.builder()
                    .userId(next.getId()).screeningId(screening.getId())
                    .seatIds(List.of(seat.getId())).build();
            assertThatThrownBy(() -> reservationService.createReserve(nextRequest))
                    .isInstanceOf(ReservationException.class);
            assertThat(paymentRepository.findByOrderId(firstOrder.getOrderId()).orElseThrow().getStatus())
                    .isEqualTo(PaymentStatus.READY);
            assertThat(reservationRepository.findByScreeningId(screening.getId()))
                    .anyMatch(reservation -> reservation.getStatus() == ReservationStatus.IN_PROGRESS);

            bookingService.expireDue();
            var nextOrder = reservationService.createReserve(nextRequest);

            // then
            assertThat(nextOrder.getOrderId()).isNotEqualTo(firstOrder.getOrderId());
            assertThat(paymentRepository.findByOrderId(firstOrder.getOrderId()).orElseThrow().getStatus())
                    .isEqualTo(PaymentStatus.EXPIRED);
            assertThat(reservationRepository.findByPaymentId(
                    paymentRepository.findByOrderId(nextOrder.getOrderId()).orElseThrow().getId()))
                    .allMatch(reservation -> reservation.getStatus() == ReservationStatus.IN_PROGRESS);
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
                    .date(LocalDate.now(clock).plusDays(1))
                    .startedAt(LocalDateTime.now(clock).plusDays(1))
                    .endedAt(LocalDateTime.now(clock).plusDays(1).plusHours(2))
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
