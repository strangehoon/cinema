package com.example.movie;

import com.example.config.IntegrationServiceTest;
import com.example.db.entity.*;
import com.example.db.enums.*;
import com.example.movie.service.CinemaCatalog;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class CinemaCatalogTest extends IntegrationServiceTest {
    @Autowired CinemaCatalog catalog;

    @Nested
    @DisplayName("회차별 잔여 좌석")
    class Availability {
        @Test
        @DisplayName("예약되지 않은 좌석만 잔여 좌석으로 집계한다")
        void success() {
            // given
            Theater theater = theaterRepository.save(Theater.builder().name("1관").build());
            Movie movie = movieRepository.save(Movie.builder().title("테스트 영화")
                    .rating(Rating.R_ALL).genre(Genre.DRAMA).releasedDate(LocalDate.now(clock))
                    .runningTimeMin(100).thumbnailImage("poster.jpg").build());
            Screening screening = screeningRepository.save(Screening.builder().movie(movie).theater(theater)
                    .date(LocalDate.now(clock).plusDays(1)).startedAt(LocalDateTime.now(clock).plusDays(1))
                    .endedAt(LocalDateTime.now(clock).plusDays(1).plusHours(2)).build());
            ScreeningSeat a1 = screeningSeatRepository.save(ScreeningSeat.builder().theater(theater).row(1).col(1).build());
            ScreeningSeat a2 = screeningSeatRepository.save(ScreeningSeat.builder().theater(theater).row(1).col(2).build());
            reservationRepository.save(Reservation.builder().screening(screening).screeningSeat(a1)
                    .status(ReservationStatus.NONE).price(12000).build());
            reservationRepository.save(Reservation.builder().screening(screening).screeningSeat(a2)
                    .status(ReservationStatus.COMPLETED).price(12000).build());

            // when
            var result = catalog.seats(screening.getId());

            // then
            assertThat(result.totalSeats()).isEqualTo(2);
            assertThat(result.availableSeats()).isEqualTo(1);
            assertThat(result.seats()).extracting(CinemaCatalog.SeatView::status)
                    .containsExactlyInAnyOrder("AVAILABLE", "COMPLETED");
        }
    }
}
