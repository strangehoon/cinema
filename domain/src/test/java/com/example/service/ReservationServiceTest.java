package com.example.service;

import com.example.config.IntegrationServiceTest;
import com.example.dto.request.ReservationServiceRequest;
import com.example.dto.response.ReservationServiceResponse;
import com.example.entity.*;
import com.example.enums.Genre;
import com.example.enums.Rating;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;
import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class ReservationServiceTest extends IntegrationServiceTest {

    @MockitoBean
    private ReservationValidator reservationValidator;

    @MockitoBean
    private ApplicationEventPublisher eventPublisher;

    @Nested
    @DisplayName("영화 좌석 예약")
    class reserveSeats {

        @Test
        @DisplayName("정상적인 요청으로 좌석 예약에 성공한다")
        void reserveSeats_success() {
            // given
            User user = saveUser();
            Theater theater = saveTheater();
            Movie movie = saveMovie();
            Screening screening = saveScreening(movie, theater);
            List<ScreeningSeat> seats = saveScreeningSeats(theater, 1);
            saveReservationsForSeats(screening, seats);

            List<Long> seatIds = seats.stream()
                    .map(ScreeningSeat::getId)
                    .toList();

            ReservationServiceRequest request = ReservationServiceRequest.builder()
                    .userId(user.getId())
                    .screeningId(screening.getId())
                    .seatIds(seatIds)
                    .build();

            // when
            ReservationServiceResponse response = reservationService.reserveSeats(request);

            // then
            assertThat(response.getUserId()).isEqualTo(user.getId());
            assertThat(response.getScreeningId()).isEqualTo(screening.getId());
            assertThat(response.getReservedSeatIds())
                    .containsExactlyInAnyOrderElementsOf(seatIds);
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
}
