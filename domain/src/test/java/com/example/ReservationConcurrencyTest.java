package com.example;

import com.example.dto.request.ReservationServiceRequest;
import com.example.entity.*;
import com.example.repository.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootTest
public class ReservationConcurrencyTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TheaterRepository theaterRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private ScreeningRepository screeningRepository;

    @Autowired
    private ScreeningSeatRepository screeningSeatRepository;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationRepository reservationRepository;

    @MockitoBean
    private ReservationValidator reservationValidator;

    @MockitoBean
    private ApplicationEventPublisher eventPublisher;

    @BeforeEach
    void setUp() {
        Movie movie = movieRepository.save(Movie.builder().build());
        Theater theater = theaterRepository.save(Theater.builder().build());
        Screening screening = screeningRepository.save(Screening.builder().movie(movie).theater(theater).build());
        ScreeningSeat seat = screeningSeatRepository.save(ScreeningSeat.builder().row(1).col(1).theater(theater).build());

        reservationRepository.save(Reservation.builder()
                .screening(screening)
                .screeningSeat(seat)
                .isReserved(false)
                .build());
    }

    @AfterEach
    void tearDown() {
        reservationRepository.deleteAll();
        screeningSeatRepository.deleteAll();
        screeningRepository.deleteAll();
        theaterRepository.deleteAll();
        movieRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("여러 사용자가 동시에 같은 좌석을 예매하면 오직 한 명만 성공해야 한다")
    void reserveSeats() throws Exception {
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger();

        Screening screening = screeningRepository.findAll().get(0);
        ScreeningSeat seat = screeningSeatRepository.findAll().get(0);

        for (int i = 0; i < threadCount; i++) {
            final Long memberId = userRepository.save(User.builder().build()).getId();

            executor.submit(() -> {
                try {
                    ReservationServiceRequest request = ReservationServiceRequest.builder()
                            .memberId(memberId)
                            .screeningId(screening.getId())
                            .seatIds(List.of(seat.getId()))
                            .build();

                    reservationService.reserveSeats(request);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    System.out.println("예외 발생: " + e.getClass() + " / " + e.getMessage());
                    e.printStackTrace(); // 추가
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        Assertions.assertEquals(1, successCount.get(), "동시에 예약 시 단 한 명만 성공해야 합니다.");
    }
}