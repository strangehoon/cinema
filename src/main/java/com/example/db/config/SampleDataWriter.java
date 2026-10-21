package com.example.db.config;

import com.example.db.entity.*;
import com.example.db.enums.Genre;
import com.example.db.enums.Rating;
import com.example.db.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Component
@Profile("!test")
@RequiredArgsConstructor
public class SampleDataWriter {
    private final MovieRepository movies;
    private final TheaterRepository theaters;
    private final ScreeningSeatRepository seats;
    private final ScreeningRepository screenings;
    private final JdbcTemplate jdbc;
    private final UserRepository users;
    private final PasswordEncoder passwords;
    private final Clock clock;

    private static final String[] TITLES = {
            "파도의 기록", "여름의 끝", "새벽의 도시", "우리가 남긴 빛", "열두 번째 밤", "궤도의 밖",
            "오래된 편지", "비밀의 정원", "푸른 그림자", "어느 겨울의 노래", "마지막 역", "오늘의 기적"
    };
    private static final Genre[] GENRES = {
            Genre.DRAMA, Genre.ROMANCE, Genre.THRILLER, Genre.DRAMA, Genre.HORROR, Genre.SF,
            Genre.DRAMA, Genre.FANTASY, Genre.ACTION, Genre.ROMANCE, Genre.MYSTERY, Genre.COMEDY
    };

    @Transactional
    public void seed() {
        LocalDate today = LocalDate.now(clock);
        if (users.findByEmail("demo@cinema.local").isEmpty()) {
            users.save(User.builder().name("체험 관객").email("demo@cinema.local")
                    .passwordHash(passwords.encode("cinema1234")).build());
        }
        if (movies.count() == 0) {
            for (int i = 0; i < TITLES.length; i++) {
                movies.save(Movie.builder().title(TITLES[i]).genre(GENRES[i])
                        .rating(i % 3 == 0 ? Rating.R_ALL : Rating.R_12)
                        .releasedDate(today.minusDays(14 - i)).runningTimeMin(95 + i * 5)
                        .thumbnailImage("").build());
            }
        }
        if (theaters.count() == 0) {
            for (int i = 1; i <= 3; i++) theaters.save(Theater.builder().name(i + "관").build());
        }
        List<Movie> movieList = movies.findAll();
        List<Theater> theaterList = theaters.findAll();
        for (Theater theater : theaterList) {
            if (seats.findByTheaterIdOrderByRowAscColAsc(theater.getId()).isEmpty()) {
                for (int row = 1; row <= 8; row++) {
                    for (int col = 1; col <= 10; col++) {
                        seats.save(ScreeningSeat.builder().theater(theater).row(row).col(col).build());
                    }
                }
            }
        }
        for (int day = 0; day < 14; day++) {
            LocalDate date = today.plusDays(day);
            for (int theaterIndex = 0; theaterIndex < theaterList.size(); theaterIndex++) {
                Theater theater = theaterList.get(theaterIndex);
                List<ScreeningSeat> theaterSeats = seats.findByTheaterIdOrderByRowAscColAsc(theater.getId());
                for (int slot = 0; slot < 3; slot++) {
                    LocalDateTime startsAt = LocalDateTime.of(date, LocalTime.of(12 + slot * 4, 0));
                    if (screenings.existsByTheaterIdAndStartedAt(theater.getId(), startsAt)) continue;
                    Movie movie = movieList.get((day * 3 + theaterIndex * 2 + slot) % movieList.size());
                    Screening screening = screenings.save(Screening.builder().date(date).startedAt(startsAt)
                            .endedAt(startsAt.plusMinutes(movie.getRunningTimeMin() + 20))
                            .movie(movie).theater(theater).build());
                    jdbc.batchUpdate("INSERT INTO reservations (status, price, screening_seat_id, screening_id, created_at, updated_at) VALUES ('NONE', ?, ?, ?, ?, ?)",
                            theaterSeats, 80, (statement, seat) -> {
                                statement.setInt(1, 10000);
                                statement.setLong(2, seat.getId());
                                statement.setLong(3, screening.getId());
                                Timestamp now = Timestamp.valueOf(LocalDateTime.now(clock));
                                statement.setTimestamp(4, now);
                                statement.setTimestamp(5, now);
                            });
                }
            }
        }
    }

}
