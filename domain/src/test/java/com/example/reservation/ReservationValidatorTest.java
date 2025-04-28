package com.example.reservation;

//import com.example.config.IntegrationServiceTest;
//import com.example.db.entity.*;
//import com.example.reservation.dto.request.ReservationServiceRequest;
//import com.example.db.enums.Genre;
//import com.example.db.enums.Rating;
//import com.example.reservation.exception.ReservationException;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Nested;
//import org.junit.jupiter.api.Test;
//import org.springframework.transaction.annotation.Transactional;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.stream.IntStream;
//import static org.junit.jupiter.api.Assertions.*;
//
//@Transactional
//class ReservationValidatorTest extends IntegrationServiceTest {
//
//    @Nested
//    @DisplayName("ReservationValidator")
//    class validate{
//
//        @Test
//        @DisplayName("모든 조건을 만족하면 예외 없이 통과한다")
//        void validate_success() {
//            // given
//            User user = saveUser();
//            Movie movie = saveMovie();
//            Theater theater = saveTheater();
//            Screening screening = saveScreening(movie, theater);
//            List<ScreeningSeat> seats = saveScreeningSeats(screening.getTheater(), 6);
//
//            List<ScreeningSeat> alreadyReservedSeats = seats.subList(0, 4);
//            saveReservationsForSeats(screening, alreadyReservedSeats, user);
//
//            ScreeningSeat sixthSeat = seats.get(4);
//            ReservationServiceRequest request = ReservationServiceRequest.builder()
//                    .userId(user.getId())
//                    .screeningId(screening.getId())
//                    .seatIds(List.of(sixthSeat.getId()))
//                    .build();
//
//            // when + then
//            assertDoesNotThrow(() -> reservationValidator.validate(request));
//        }
//
//
//        @Test
//        @DisplayName("1인당 5좌석 초과 요청 시 예외가 발생한다.")
//        void validate_fail_exceedMaxSeats() {
//            // given
//            User user = saveUser();
//            Movie movie = saveMovie();
//            Theater theater = saveTheater();
//            Screening screening = saveScreening(movie, theater);
//            List<ScreeningSeat> seats = saveScreeningSeats(screening.getTheater(), 6);
//
//            List<ScreeningSeat> alreadyReservedSeats = seats.subList(0, 5);
//            saveReservationsForSeats(screening, alreadyReservedSeats, user);
//
//            ScreeningSeat sixthSeat = seats.get(5);
//            ReservationServiceRequest request = ReservationServiceRequest.builder()
//                    .userId(user.getId())
//                    .screeningId(screening.getId())
//                    .seatIds(List.of(sixthSeat.getId()))
//                    .build();
//
//            // when & then
//            ReservationException exception = assertThrows(ReservationException.class, () -> {
//                reservationValidator.validate(request);
//            });
//            assertEquals("1인당 최대 5좌석까지만 예매할 수 있습니다.", exception.getMessage());
//        }
//
//        @Test
//        @DisplayName("요청한 좌석이 유효하지 않다면 예외가 발생한다.")
//        void validate_fail_wrongSeats() {
//            // given
//            User user = saveUser();
//            Movie movie = saveMovie();
//            Theater theater = saveTheater();
//            Screening screening = saveScreening(movie, theater);
//
//            ReservationServiceRequest request = ReservationServiceRequest.builder()
//                    .userId(user.getId())
//                    .screeningId(screening.getId())
//                    .seatIds(List.of(1L))
//                    .build();
//
//            // when & then
//            ReservationException exception = assertThrows(ReservationException.class, () -> {
//                reservationValidator.validate(request);
//            });
//            assertEquals("유효하지 않은 좌석이 포함되어 있습니다.", exception.getMessage());
//        }
//
//        @Test
//        @DisplayName("다른 row의 좌석을 요청하면 예외가 발생한다.")
//        void validate_fail_differentRow() {
//            // given
//            User user = saveUser();
//            Movie movie = saveMovie();
//            Theater theater = saveTheater();
//            Screening screening = saveScreening(movie, theater);
//            ScreeningSeat seat1 = saveScreeningSeat(theater, 1, 1);
//            ScreeningSeat seat2 = saveScreeningSeat(theater, 2, 1);
//
//            ReservationServiceRequest request = ReservationServiceRequest.builder()
//                    .userId(user.getId())
//                    .screeningId(screening.getId())
//                    .seatIds(List.of(seat1.getId(), seat2.getId()))
//                    .build();
//
//            // when & then
//            ReservationException exception = assertThrows(ReservationException.class, () -> {
//                reservationValidator.validate(request);
//            });
//            assertEquals("좌석은 같은 행(row)이어야 합니다.", exception.getMessage());
//        }
//
//        @Test
//        @DisplayName("좌석이 인접하지 않으면 예외 발생")
//        void validate_fail_notAdjacent() {
//            // given
//            User user = saveUser();
//            Movie movie = saveMovie();
//            Theater theater = saveTheater();
//            Screening screening = saveScreening(movie, theater);
//            ScreeningSeat seat1 = saveScreeningSeat(theater, 1, 1);
//            ScreeningSeat seat2 = saveScreeningSeat(theater, 1, 3);
//
//            ReservationServiceRequest request = ReservationServiceRequest.builder()
//                    .userId(user.getId())
//                    .screeningId(screening.getId())
//                    .seatIds(List.of(seat1.getId(), seat2.getId()))
//                    .build();
//
//            // when & then
//            ReservationException exception = assertThrows(ReservationException.class, () -> {
//                reservationValidator.validate(request);
//            });
//            assertEquals("좌석은 같은 행에서 인접해야 합니다.", exception.getMessage());
//        }
//    }
//
//    private User saveUser() {
//        return userRepository.save(User.builder()
//                .name("user1")
//                .build());
//    }
//
//    private Movie saveMovie() {
//        return movieRepository.save(Movie.builder()
//                .title("movie1")
//                .rating(Rating.R_12)
//                .releasedDate(LocalDate.of(2024, 1, 1))
//                .thumbnailImage("movie1-thumbnail.jpg")
//                .runningTimeMin(120)
//                .genre(Genre.DRAMA)
//                .build());
//    }
//
//    private Theater saveTheater() {
//        return theaterRepository.save(Theater.builder()
//                .name("theater1")
//                .build());
//    }
//
//    private Screening saveScreening(Movie movie, Theater theater) {
//        return screeningRepository.save(Screening.builder()
//                .date(LocalDate.of(2024, 1, 1))
//                .startedAt(LocalDateTime.of(2024, 1, 1, 14, 0))
//                .endedAt(LocalDateTime.of(2024, 1, 1, 16, 0))
//                .movie(movie)
//                .theater(theater)
//                .build());
//    }
//
//    private List<ScreeningSeat> saveScreeningSeats(Theater theater, int count) {
//        return IntStream.range(0, count)
//                .mapToObj(i -> ScreeningSeat.builder()
//                        .row(1)
//                        .col(i + 1)
//                        .theater(theater)
//                        .build())
//                .map(screeningSeatRepository::save)
//                .toList();
//    }
//
//    private ScreeningSeat saveScreeningSeat(Theater theater, int row, int col) {
//        return screeningSeatRepository.save(ScreeningSeat.builder()
//                .theater(theater)
//                .row(row)
//                .col(col)
//                .build());
//    }
//
//    private List<Reservation> saveReservationsForSeats(Screening screening, List<ScreeningSeat> seats, User user) {
//        return seats.stream()
//                .map(seat -> Reservation.builder()
//                        .screening(screening)
//                        .screeningSeat(seat)
//                        .isReserved(true)
//                        .user(user)
//                        .build())
//                .map(reservationRepository::save)
//                .toList();
//    }
//}