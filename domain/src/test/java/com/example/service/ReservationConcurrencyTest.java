package com.example.service;

import com.example.config.IntegrationServiceTest;
import com.example.dto.request.ReservationServiceRequest;
import com.example.entity.*;
import com.example.enums.Genre;
import com.example.enums.Rating;
import org.junit.jupiter.api.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReservationConcurrencyTest extends IntegrationServiceTest {

    @MockitoBean
    private ReservationValidator reservationValidator;

    @MockitoBean
    private ApplicationEventPublisher eventPublisher;

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
        reservationRepository.deleteAllInBatch();
        screeningSeatRepository.deleteAllInBatch();
        screeningRepository.deleteAllInBatch();
        theaterRepository.deleteAllInBatch();
        movieRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @Nested
    @DisplayName("영화 좌석 예약")
    class reserveSeats {

        @Test
        @DisplayName("여러 사용자가 동시에 같은 좌석을 예매하면 오직 한 명만 성공해야 한다")
        void reserveSeats_success_concurrency() throws Exception {
            // given
            Movie movie = saveMovie();
            Theater theater = saveTheater();
            Screening screening = saveScreening(movie, theater);
            List<ScreeningSeat> seats = saveScreeningSeats(theater, 1);
            List<Reservation> reservations = saveReservationsForSeats(screening, seats);

            List<Long> seatIds = seats.stream()
                    .map(ScreeningSeat::getId)
                    .toList();

            int threadCount = 10;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);
            AtomicInteger successCount = new AtomicInteger();

            // when
            for (int i = 0; i < threadCount; i++) {
                User user = saveUser();

                executor.submit(() -> {
                    try {
                        ReservationServiceRequest request = ReservationServiceRequest.builder()
                                .userId(user.getId())
                                .screeningId(screening.getId())
                                .seatIds(seatIds)
                                .build();

                        reservationService.reserveSeats(request);
                        successCount.incrementAndGet();
                    } catch (Exception e) {
                        System.out.println("예외 발생: " + e.getClass() + " / " + e.getMessage());
                    } finally {
                        latch.countDown();
                    }
                });
            }
            latch.await();

            // then
            assertEquals(1, successCount.get(), "동시에 예약 시 단 한 명만 성공해야 합니다.");
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
                        .isReserved(false)
                        .build())
                .map(reservationRepository::save)
                .toList();
    }
}