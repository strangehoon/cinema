package com.example.repository;

import com.example.entity.Movie;
import com.example.enums.Genre;
import java.util.List;

public interface MovieRepositoryCustom {

    List<Movie> searchMoviesWithScreenings(String title, Genre genre);
}
