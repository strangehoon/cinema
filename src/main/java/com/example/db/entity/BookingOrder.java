package com.example.db.entity;

import com.example.db.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "booking_orders")
public class BookingOrder extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false, unique = true, length = 100)
    private String orderId;

    @Column(name = "order_name", length = 50)
    private String orderName;

    @Column(name = "total_amount")
    private Long totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private PaymentStatus status;

    private LocalDateTime expiresAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public BookingOrder(String orderId, String orderName, Long totalAmount, PaymentStatus status, User user) {
        this.orderId = orderId;
        this.orderName = orderName;
        this.totalAmount = totalAmount;
        this.status = status;
        this.user = user;
    }

    public void setStatus(PaymentStatus status) { this.status = status; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
}
