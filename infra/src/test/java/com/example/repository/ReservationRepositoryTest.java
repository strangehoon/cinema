package com.example.repository;

import com.example.config.IntegrationRepositoryTest;
import com.example.db.entity.*;
import com.example.db.enums.Genre;
import com.example.db.enums.Rating;
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
        @DisplayName("특정 사용자의 상영 회차 예약 횟수를 조회한다")
        void success() {
            // given
            User user = saveUser();
            Movie movie = saveMovie();
            Theater theater = saveTheater();
            Screening screening = saveScreening(movie, theater);
            ScreeningSeat screeningSeat = saveScreeningSeat(screening, theater);
            saveReservation(screeningSeat, screening, user);

            // when
            long count = reservationRepository.countByUserIdAndScreeningId(user.getId(), screening.getId());

            // then
            assertThat(count).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("findByScreeningIdAndScreeningSeatIdInWithLock 메서드")
    class FindByScreeningIdAndScreeningSeatIdInWithLock {

        @Test
        @DisplayName("screeningId와 seatIds로 예약 정보를 조회한다. (version 컬럼 포함)")
        void success() {
            // given
            Movie movie = saveMovie();
            Theater theater = saveTheater();
            Screening screening = saveScreening(movie, theater);
            ScreeningSeat screeningSeat = saveScreeningSeat(screening, theater);
            Reservation reservation = saveReservation(screeningSeat, screening, null);

            // when
            List<Reservation> reservations = reservationRepository.findByScreeningIdAndScreeningSeatIdInWithLock(
                    screening.getId(),
                    List.of(screeningSeat.getId())
            );

            // then
            assertThat(reservations).hasSize(1);
            assertThat(reservations.get(0).getId()).isEqualTo(reservation.getId());
            assertThat(reservations.get(0).getVersion()).isEqualTo(0);
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

    private ScreeningSeat saveScreeningSeat(Screening screening, Theater theater) {
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
}