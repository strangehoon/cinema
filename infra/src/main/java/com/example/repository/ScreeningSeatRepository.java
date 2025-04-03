package com.example.repository;

import com.example.entity.ScreeningSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ScreeningSeatRepository extends JpaRepository<ScreeningSeat, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM ScreeningSeat s WHERE s.id IN :seatIds")
    List<ScreeningSeat> findByIdInWithLock(@Param("seatIds") List<Long> seatIds);
}
