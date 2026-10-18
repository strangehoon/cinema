package com.example.movie.service;

import com.example.db.entity.Movie;
import com.example.db.entity.Reservation;
import com.example.db.entity.Screening;
import com.example.db.enums.Genre;
import com.example.db.enums.ReservationStatus;
import com.example.db.repository.MovieRepository;
import com.example.db.repository.ReservationRepository;
import com.example.db.repository.ScreeningRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CinemaCatalog {
    private final MovieRepository movies;
    private final ScreeningRepository screenings;
    private final ReservationRepository reservations;
    private final MovieListCache listCache;

    public record MovieView(Long id, String title, String genre, String rating, int runningTime,
                            String poster, List<ScreeningView> screenings) {}
    public record ScreeningView(Long id, Long movieId, String movieTitle, String theater,
                                LocalDateTime startsAt, LocalDateTime endsAt,
                                int totalSeats, int availableSeats) {}
    public record SeatView(Long id, int row, int col, int price, String status) {}
    public record SeatMap(ScreeningView screening, int totalSeats, int availableSeats, List<SeatView> seats) {}
    public record MoviePage(List<MovieView> content, long totalElements, int page, int size) {}

    public MoviePage movies(String title, String genre, int page, int size) {
        Genre genreFilter = genre == null || genre.isBlank() ? null : Genre.valueOf(genre.toUpperCase());
        int safePage = Math.max(0, page);
        int safeSize = Math.clamp(size, 1, 30);
        var result = listCache.search(title, genreFilter, safePage, safeSize);
        if (result.ids().isEmpty()) return new MoviePage(List.of(), result.total(), safePage, safeSize);
        Map<Long, Movie> found = movies.findAllWithScreeningsByIdIn(result.ids()).stream()
                .collect(Collectors.toMap(Movie::getId, Function.identity()));
        List<Movie> ordered = result.ids().stream().map(found::get).filter(java.util.Objects::nonNull).toList();
        Map<Long, int[]> counts = counts(ordered.stream().flatMap(m -> m.getScreenings().stream())
                .map(Screening::getId).toList());
        return new MoviePage(ordered.stream().map(m -> movieView(m, counts)).toList(),
                result.total(), safePage, safeSize);
    }

    public MovieView movie(Long id) {
        Movie movie = movies.findAllWithScreeningsByIdIn(List.of(id)).stream().findFirst()
                .orElseThrow(() -> new IllegalArgumentException("영화를 찾을 수 없습니다."));
        return movieView(movie, counts(movie.getScreenings().stream().map(Screening::getId).toList()));
    }

    public SeatMap seats(Long screeningId) {
        Screening screening = screenings.findById(screeningId)
                .orElseThrow(() -> new IllegalArgumentException("상영 회차를 찾을 수 없습니다."));
        List<Reservation> inventory = reservations.findByScreeningId(screeningId);
        List<SeatView> seats = inventory.stream().map(r -> new SeatView(r.getScreeningSeat().getId(),
                r.getScreeningSeat().getRow(), r.getScreeningSeat().getCol(), r.getPrice(),
                r.getStatus() == ReservationStatus.NONE ? "AVAILABLE" : r.getStatus().name())).toList();
        long available = seats.stream().filter(seat -> seat.status().equals("AVAILABLE")).count();
        return new SeatMap(screeningView(screening, inventory.size(), (int) available),
                seats.size(), (int) available, seats);
    }

    private MovieView movieView(Movie movie, Map<Long, int[]> counts) {
        List<ScreeningView> sessions = movie.getScreenings().stream()
                .filter(s -> s.getStartedAt().isAfter(LocalDateTime.now()))
                .map(s -> {
                    int[] stock = counts.getOrDefault(s.getId(), new int[] {0, 0});
                    return screeningView(s, stock[0], stock[1]);
                }).toList();
        return new MovieView(movie.getId(), movie.getTitle(), movie.getGenre().name(),
                movie.getRating().name(), movie.getRunningTimeMin(), movie.getThumbnailImage(), sessions);
    }

    private Map<Long, int[]> counts(List<Long> ids) {
        if (ids.isEmpty()) return Map.of();
        return reservations.availabilityByScreeningIds(ids, ReservationStatus.NONE).stream()
                .collect(Collectors.toMap(row -> (Long) row[0],
                        row -> new int[] {((Number) row[1]).intValue(), ((Number) row[2]).intValue()}));
    }

    private ScreeningView screeningView(Screening screening, int total, int available) {
        return new ScreeningView(screening.getId(), screening.getMovie().getId(), screening.getMovie().getTitle(),
                screening.getTheater().getName(), screening.getStartedAt(), screening.getEndedAt(), total, available);
    }
}
