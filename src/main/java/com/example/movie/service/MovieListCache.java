package com.example.movie.service;

import com.example.db.enums.Genre;
import com.example.db.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MovieListCache {
    private final MovieRepository movies;
    private final StringRedisTemplate redis;

    public record SearchResult(List<Long> ids, long total) {}

    public SearchResult search(String title, Genre genre, int page, int size) {
        String key = "movies:v1:" + (title == null ? "" : title.trim()) + ":" +
                (genre == null ? "all" : genre.name()) + ":" + page + ":" + size;
        try {
            String cached = redis.opsForValue().get(key);
            if (cached != null) {
                String[] parts = cached.split(";", 2);
                List<Long> ids = parts.length < 2 || parts[1].isBlank() ? List.of() :
                        Arrays.stream(parts[1].split(",")).map(Long::parseLong).toList();
                return new SearchResult(ids, Long.parseLong(parts[0]));
            }
        } catch (RuntimeException ignored) {
            // Cache outages must not prevent movie browsing.
        }
        var result = movies.searchMoviesWithScreenings(title, genre, PageRequest.of(page, size));
        SearchResult fresh = new SearchResult(result.getContent().stream().map(movie -> movie.getId()).toList(),
                result.getTotalElements());
        try {
            redis.opsForValue().set(key, fresh.total() + ";" +
                    String.join(",", fresh.ids().stream().map(String::valueOf).toList()), Duration.ofSeconds(60));
        } catch (RuntimeException ignored) {
            // DB search remains the source of truth.
        }
        return fresh;
    }
}
