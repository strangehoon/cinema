package com.example.repository;

import com.example.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    int countByUserIdAndScreeningId(Long userId, Long screeningId);

    List<Reservation> findByScreeningIdAndScreeningSeatIdIn(Long screeningId, List<Long> seatIds);
}
