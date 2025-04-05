package com.example.repository;

import com.example.entity.Movie;
import com.example.enums.Genre;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MovieRepositoryCustom {

    Page<Movie> searchMoviesWithScreenings(String title, Genre genre, Long theaterId, Pageable pageable);
}
