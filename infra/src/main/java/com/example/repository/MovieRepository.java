package com.example.repository;

import com.example.entity.Screening;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieRepository extends JpaRepository<Screening, Long>, MovieRepositoryCustom{
}
