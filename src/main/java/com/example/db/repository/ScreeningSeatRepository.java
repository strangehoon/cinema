package com.example.db.repository;

import com.example.db.entity.ScreeningSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ScreeningSeatRepository extends JpaRepository<ScreeningSeat, Long> {
    List<ScreeningSeat> findByTheaterIdOrderByRowAscColAsc(Long theaterId);
}
