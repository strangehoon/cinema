package com.example.db.repository;

import com.example.db.entity.Reservation;
import com.example.db.enums.PaymentStatus;
import com.example.db.enums.ReservationStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
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

    @Query("SELECT DISTINCT p.id FROM Reservation r JOIN r.payment p " +
            "WHERE r.id IN :reservationIds AND p.order.status = :status " +
            "AND p.order.expiresAt IS NOT NULL AND p.order.expiresAt <= :now")
    List<Long> findExpiredPaymentIds(@Param("reservationIds") List<Long> reservationIds,
                                     @Param("status") PaymentStatus status,
                                     @Param("now") LocalDateTime now);

    List<Reservation> findByPaymentId(Long paymentId);
    List<Reservation> findByScreeningId(Long screeningId);
    List<Reservation> findByUserIdOrderByIdDesc(Long userId);
    @Query("select r.screening.id, count(r), sum(case when r.status = :available then 1 else 0 end) " +
            "from Reservation r where r.screening.id in :screeningIds group by r.screening.id")
    List<Object[]> availabilityByScreeningIds(@Param("screeningIds") List<Long> screeningIds,
                                               @Param("available") ReservationStatus available);
}
