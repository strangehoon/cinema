package com.example.db.repository;

import com.example.db.entity.BookingOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingOrderRepository extends JpaRepository<BookingOrder, Long> {
}
