package com.example.reservation;

import com.example.config.IntegrationServiceTest;
import com.example.db.entity.Movie;
import com.example.db.entity.Reservation;
import com.example.db.entity.Screening;
import com.example.db.entity.ScreeningSeat;
import com.example.db.entity.Theater;
import com.example.db.entity.User;
import com.example.db.repository.BookingSeatRepository;
import com.example.db.enums.Genre;
import com.example.db.enums.Rating;
import com.example.db.enums.ReservationStatus;
import com.example.reservation.dto.request.ReservationCreateServiceRequest;
import com.example.reservation.exception.ReservationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class ReservationConcurrencyTest extends IntegrationServiceTest {
    @Autowired
    private DataSource dataSource;

    @Autowired
    private BookingSeatRepository bookingSeatRepository;

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
    @DisplayName("좌석 예매 동시성")
    class ConcurrentBooking {
        @Test
        @DisplayName("같은 좌석 요청은 재고 행 잠금 후 한 요청만 성공한다")
        void success_only_one_request_can_reserve_same_seat() throws Exception {
            // given
            User firstUser = saveUser("first");
            User secondUser = saveUser("second");
            Theater theater = saveTheater();
            Screening screening = saveScreening(theater);
            ScreeningSeat seat = saveSeats(theater, 1).getFirst();
            Reservation inventory = saveInventory(screening, List.of(seat)).getFirst();

            // when
            List<Boolean> outcomes = runWhileRowLocked(inventory.getId(), List.of(
                    () -> reserve(firstUser, screening, seat),
                    () -> reserve(secondUser, screening, seat)));

            // then
            assertThat(outcomes).containsExactlyInAnyOrder(true, false);
            assertThat(reservationRepository.findById(inventory.getId()).orElseThrow().getStatus())
                    .isEqualTo(ReservationStatus.IN_PROGRESS);
            assertThat(paymentRepository.count()).isEqualTo(1);
            assertThat(bookingSeatRepository.count()).isEqualTo(1);
        }

        @Test
        @DisplayName("같은 사용자의 서로 다른 좌석 요청도 사용자별 최대 3석을 넘지 않는다")
        void success_same_user_concurrent_requests_respect_three_seat_limit() throws Exception {
            // given
            User user = saveUser("same-user");
            Theater theater = saveTheater();
            Screening screening = saveScreening(theater);
            List<ScreeningSeat> seats = saveSeats(theater, 4);
            saveInventory(screening, seats);
            reserve(user, screening, seats.get(0), seats.get(1));

            // when
            List<Boolean> outcomes = runWhileUserLocked(user.getId(), List.of(
                    () -> reserve(user, screening, seats.get(2)),
                    () -> reserve(user, screening, seats.get(3))));

            // then
            assertThat(outcomes).containsExactlyInAnyOrder(true, false);
            assertThat(reservationRepository.countByUserIdAndScreeningId(user.getId(), screening.getId()))
                    .isEqualTo(3);
            assertThat(paymentRepository.count()).isEqualTo(2);
        }

        private List<Boolean> runWhileRowLocked(Long reservationId, List<Supplier<Boolean>> requests) throws Exception {
            return runWhileLocked("SELECT id FROM reservations WHERE id = ? FOR UPDATE", reservationId, requests);
        }

        private List<Boolean> runWhileUserLocked(Long userId, List<Supplier<Boolean>> requests) throws Exception {
            return runWhileLocked("SELECT id FROM users WHERE id = ? FOR UPDATE", userId, requests);
        }

        private List<Boolean> runWhileLocked(String sql, Long id, List<Supplier<Boolean>> requests) throws Exception {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {
                connection.setAutoCommit(false);
                statement.setLong(1, id);
                try (ResultSet ignored = statement.executeQuery()) {
                    assertThat(ignored.next()).isTrue();
                }

                CountDownLatch ready = new CountDownLatch(requests.size());
                CountDownLatch start = new CountDownLatch(1);
                ExecutorService executor = Executors.newFixedThreadPool(requests.size());
                try {
                    List<Future<Boolean>> futures = requests.stream().map(request -> executor.submit(() -> {
                        ready.countDown();
                        start.await();
                        return request.get();
                    })).toList();
                    assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
                    start.countDown();
                    Thread.sleep(300);
                    assertThat(futures).allMatch(future -> !future.isDone());

                    connection.commit();
                    return futures.stream().map(future -> getOutcome(future)).toList();
                } finally {
                    if (!connection.getAutoCommit()) connection.rollback();
                    executor.shutdownNow();
                    assertThat(executor.awaitTermination(5, TimeUnit.SECONDS)).isTrue();
                }
            }
        }

        private boolean getOutcome(Future<Boolean> future) {
            try {
                return future.get(10, TimeUnit.SECONDS);
            } catch (java.util.concurrent.ExecutionException exception) {
                if (exception.getCause() instanceof ReservationException) return false;
                throw new AssertionError("Concurrent reservation failed unexpectedly: " + exception.getCause(),
                        exception.getCause());
            } catch (Exception exception) {
                throw new AssertionError(exception);
            }
        }

        private boolean reserve(User user, Screening screening, ScreeningSeat... seats) {
            try {
                reservationService.createReserve(ReservationCreateServiceRequest.builder()
                        .userId(user.getId())
                        .screeningId(screening.getId())
                        .seatIds(java.util.Arrays.stream(seats).map(ScreeningSeat::getId).toList())
                        .build());
                return true;
            } catch (ReservationException exception) {
                return false;
            }
        }

        private User saveUser(String name) {
            return userRepository.save(User.builder().name(name).build());
        }

        private Theater saveTheater() {
            return theaterRepository.save(Theater.builder().name("theater").build());
        }

        private Screening saveScreening(Theater theater) {
            Movie movie = movieRepository.save(Movie.builder()
                    .title("movie")
                    .rating(Rating.R_12)
                    .releasedDate(LocalDate.of(2024, 1, 1))
                    .thumbnailImage("movie.svg")
                    .runningTimeMin(120)
                    .genre(Genre.DRAMA)
                    .build());
            return screeningRepository.save(Screening.builder()
                    .date(LocalDate.now(clock).plusDays(1))
                    .startedAt(LocalDateTime.now(clock).plusDays(1))
                    .endedAt(LocalDateTime.now(clock).plusDays(1).plusHours(2))
                    .movie(movie)
                    .theater(theater)
                    .build());
        }

        private List<ScreeningSeat> saveSeats(Theater theater, int count) {
            return IntStream.range(0, count)
                    .mapToObj(index -> screeningSeatRepository.save(ScreeningSeat.builder()
                            .row(1).col(index + 1).theater(theater).build()))
                    .toList();
        }

        private List<Reservation> saveInventory(Screening screening, List<ScreeningSeat> seats) {
            return seats.stream().map(seat -> reservationRepository.save(Reservation.builder()
                    .screening(screening).screeningSeat(seat).price(10000)
                    .status(ReservationStatus.NONE).build())).toList();
        }
    }
}
