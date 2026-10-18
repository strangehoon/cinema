package com.example.db.repository;

import com.example.db.entity.Payment;
import com.example.db.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;
import com.example.db.enums.PaymentStatus;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @Query("select p from Payment p where p.order.orderId = :orderId")
    Optional<Payment> findByOrderId(@Param("orderId") String orderId);

    @Query("select p from Payment p where p.order.user.id = :userId order by p.id desc")
    List<Payment> findByUserIdOrderByIdDesc(@Param("userId") Long userId);

    @Query("select p from Payment p where p.order.status = :status and p.order.expiresAt < :before")
    List<Payment> findByStatusAndExpiresAtBefore(@Param("status") PaymentStatus status,
                                                  @Param("before") LocalDateTime before);

    @Query("select p from Payment p where p.order.status = :status")
    List<Payment> findByStatus(@Param("status") PaymentStatus status);
}
