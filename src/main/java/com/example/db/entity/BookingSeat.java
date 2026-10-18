package com.example.db.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "booking_seats")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BookingSeat {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "booking_order_id", nullable = false)
    private BookingOrder order;
    private Long screeningId;
    private LocalDateTime startsAt;
    private String seatLabel;
    private int price;

    public BookingSeat(BookingOrder order, Long screeningId, LocalDateTime startsAt, String seatLabel, int price) {
        this.order = order;
        this.screeningId = screeningId;
        this.startsAt = startsAt;
        this.seatLabel = seatLabel;
        this.price = price;
    }
}
