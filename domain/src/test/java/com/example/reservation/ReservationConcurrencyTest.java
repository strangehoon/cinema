package com.example.reservation;

import com.example.config.IntegrationServiceTest;
import com.example.db.entity.*;
import com.example.db.enums.ReservationStatus;
import com.example.reservation.dto.request.ReservationServiceRequest;
import com.example.db.enums.Genre;
import com.example.db.enums.Rating;
import com.example.reservation.service.ReservationValidator;
import org.junit.jupiter.api.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
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
            List<ScreeningSeat> seats = saveScreeningSeats(theater, 3);
            List<Reservation> reservations = saveReservationsForSeats(screening, seats);

            List<Long> seatIds = seats.stream()
                    .map(ScreeningSeat::getId)
                    .toList();

            int threadCount = 100;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);
            AtomicInteger successCount = new AtomicInteger();
            List<Long> responseTimes = Collections.synchronizedList(new ArrayList<>());

            // when
            for (int i = 0; i < threadCount; i++) {
                User user = saveUser();

                executor.submit(() -> {
                    long startTime = System.currentTimeMillis();
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
                        long endTime = System.currentTimeMillis();
                        responseTimes.add(endTime - startTime);
                        latch.countDown();
                    }
                });
            }
            latch.await();

            // then
            assertEquals(1, successCount.get(), "동시에 예약 시 단 한 명만 성공해야 합니다.");
            double averageResponseTime = responseTimes.stream()
                    .mapToLong(Long::longValue)
                    .average()
                    .orElse(0.0);

            System.out.println("✅ 평균 응답 시간: " + averageResponseTime + "ms");
        }
    }

    @Test
    @DisplayName("seatIds 순서를 달리하면 데드락 발생 가능성 있음")
    void reserveSeats_deadlock_possible_with_inconsistent_lock_order() throws Exception {
        // given
        Movie movie = saveMovie();
        Theater theater = saveTheater();
        Screening screening = saveScreening(movie, theater);
        List<ScreeningSeat> seats = saveScreeningSeats(theater, 2); // 좌석 2개

        List<Reservation> reservations = saveReservationsForSeats(screening, seats);
        Long seatId1 = seats.get(0).getId();
        Long seatId2 = seats.get(1).getId();

        User userA = saveUser();
        User userB = saveUser();

        // 서로 다른 순서로 좌석 예약 요청
        ReservationServiceRequest requestA = ReservationServiceRequest.builder()
                .userId(userA.getId())
                .screeningId(screening.getId())
                .seatIds(List.of(seatId1, seatId2)) // 1, 2
                .build();

        ReservationServiceRequest requestB = ReservationServiceRequest.builder()
                .userId(userB.getId())
                .screeningId(screening.getId())
                .seatIds(List.of(seatId2, seatId1)) // 2, 1
                .build();

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(2);
        AtomicInteger successCount = new AtomicInteger();

        executor.submit(() -> {
            try {
                reservationService.reserveSeats(requestA);
                successCount.incrementAndGet();
            } catch (Exception e) {
                System.out.println("❌ User A 예외: " + e.getClass() + " / " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });

        executor.submit(() -> {
            try {
                reservationService.reserveSeats(requestB);
                successCount.incrementAndGet();
            } catch (Exception e) {
                System.out.println("❌ User B 예외: " + e.getClass() + " / " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });

        // then
        latch.await();
        assertEquals(1, successCount.get(), "둘 중 하나만 예약에 성공해야 하며, 데드락이 발생하지 않아야 합니다.");
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
                        .status(ReservationStatus.NONE)
                        .price(10000)
                        .screening(screening)
                        .screeningSeat(seat)
                        .payment(null)
                        .user(null)
                        .build())
                .map(reservationRepository::save)
                .toList();
    }
}