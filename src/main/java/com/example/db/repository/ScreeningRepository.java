package com.example.db.repository;

import com.example.db.entity.Screening;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScreeningRepository extends JpaRepository<Screening, Long> {
    boolean existsByTheaterIdAndStartedAt(Long theaterId, java.time.LocalDateTime startedAt);
}
