package com.example.db.repository;

import com.example.db.entity.Reservation;
import com.example.db.enums.ReservationStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    int countByUserIdAndScreeningId(Long userId, Long screeningId);

    @Query("SELECT r FROM Reservation r WHERE r.screening.id = :screeningId AND r.screeningSeat.id IN :seatIds")
    List<Reservation> findByScreeningIdAndScreeningSeatId(@Param("screeningId") Long screeningId, @Param("seatIds") List<Long> seatIds);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Reservation r WHERE r.screening.id = :screeningId " +
            "AND r.screeningSeat.id IN :seatIds ORDER BY r.screeningSeat.id ASC")
    List<Reservation> findByScreeningIdAndScreeningSeatIdForUpdate(
            @Param("screeningId") Long screeningId, @Param("seatIds") List<Long> seatIds);

    List<Reservation> findByPaymentId(Long paymentId);
    List<Reservation> findByScreeningId(Long screeningId);
    List<Reservation> findByUserIdOrderByIdDesc(Long userId);
    @Query("select r.screening.id, count(r), sum(case when r.status = :available then 1 else 0 end) " +
            "from Reservation r where r.screening.id in :screeningIds group by r.screening.id")
    List<Object[]> availabilityByScreeningIds(@Param("screeningIds") List<Long> screeningIds,
                                               @Param("available") ReservationStatus available);
}
