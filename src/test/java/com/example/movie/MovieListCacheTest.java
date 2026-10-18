package com.example.movie;

import com.example.db.entity.Movie;
import com.example.db.enums.Genre;
import com.example.db.repository.MovieRepository;
import com.example.movie.service.MovieListCache;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class MovieListCacheTest {
    private final MovieRepository movies = mock(MovieRepository.class);
    private final StringRedisTemplate redis = mock(StringRedisTemplate.class);
    private final MovieListCache cache = new MovieListCache(movies, redis);

    @Nested
    @DisplayName("영화 목록 캐시")
    class Search {
        @Test
        @DisplayName("Redis 연결에 실패해도 DB 검색 결과를 제공한다")
        void success_redis_unavailable() {
            // given
            Movie movie = mock(Movie.class);
            when(movie.getId()).thenReturn(3L);
            when(redis.opsForValue()).thenThrow(new IllegalStateException("Redis down"));
            when(movies.searchMoviesWithScreenings(eq("파도"), eq(Genre.DRAMA), any()))
                    .thenReturn(new PageImpl<>(List.of(movie), PageRequest.of(0, 12), 1));

            // when
            var result = cache.search("파도", Genre.DRAMA, 0, 12);

            // then
            assertThat(result.ids()).containsExactly(3L);
            assertThat(result.total()).isEqualTo(1);
        }
    }
}
