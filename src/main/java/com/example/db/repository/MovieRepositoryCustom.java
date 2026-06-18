package com.example.db.repository;

import com.example.db.entity.Movie;
import com.example.db.enums.Genre;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MovieRepositoryCustom {

    Page<Movie> searchMoviesWithScreenings(String title, Genre genre, Pageable pageable);
}
