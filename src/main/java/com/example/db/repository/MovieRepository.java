package com.example.db.repository;

import com.example.db.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieRepository extends JpaRepository<Movie, Long>, MovieRepositoryCustom{
}
