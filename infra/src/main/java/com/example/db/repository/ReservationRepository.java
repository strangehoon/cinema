package com.example.db.repository;

import com.example.db.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    int countByUserIdAndScreeningId(Long userId, Long screeningId);

    @Query("SELECT r FROM Reservation r WHERE r.screening.id = :screeningId AND r.screeningSeat.id IN :seatIds")
    List<Reservation> findByScreeningIdAndScreeningSeatId(@Param("screeningId") Long screeningId, @Param("seatIds") List<Long> seatIds);

    Optional<List<Reservation>> findByPaymentId(Long paymentId);
}