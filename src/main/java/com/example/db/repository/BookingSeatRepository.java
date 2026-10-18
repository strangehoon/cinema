package com.example.db.repository;

import com.example.db.entity.BookingSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface BookingSeatRepository extends JpaRepository<BookingSeat, Long> {
    @Query("select s from BookingSeat s where s.order.id = :orderId order by s.id")
    List<BookingSeat> findByOrderIdOrderById(@Param("orderId") Long orderId);
}
