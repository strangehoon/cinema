package com.example.db.entity;

import com.example.db.enums.PaymentMethod;
import com.example.db.enums.PaymentStatus;
import com.example.db.enums.PaymentType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "payments")
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 200)
    private String paymentKey;

    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    private PaymentType type;

    @OneToOne(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE}, optional = false)
    @JoinColumn(name = "booking_order_id", nullable = false, unique = true)
    private BookingOrder order;

    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    private PaymentMethod method;

    private LocalDateTime requestedAt;

    private LocalDateTime approvedAt;

    @Builder
    private Payment(String paymentKey, PaymentType type, String orderId, String orderName, Long totalAmount,
                   PaymentStatus status, PaymentMethod method, LocalDateTime requestedAt,
                   LocalDateTime approvedAt, User user) {
        this.paymentKey = paymentKey;
        this.type = type;
        this.order = new BookingOrder(orderId, orderName, totalAmount, status, user);
        this.method = method;
        this.requestedAt = requestedAt;
        this.approvedAt = approvedAt;
    }

    public String getOrderId() { return order.getOrderId(); }
    public String getOrderName() { return order.getOrderName(); }
    public Long getTotalAmount() { return order.getTotalAmount(); }
    public PaymentStatus getStatus() { return order.getStatus(); }
    public LocalDateTime getExpiresAt() { return order.getExpiresAt(); }
    public User getUser() { return order.getUser(); }

    public void setExpiresAt(LocalDateTime expiresAt) { order.setExpiresAt(expiresAt); }
    public void setStatus(PaymentStatus status) { order.setStatus(status); }
    public void setPaymentKey(String paymentKey) { this.paymentKey = paymentKey; }

    public static Payment of(String orderId, String orderName, Long totalAmount,
                             PaymentStatus status, User user) {
        return Payment.builder()
                .orderId(orderId)
                .orderName(orderName)
                .totalAmount(totalAmount)
                .status(status)
                .user(user)
                .build();
    }
}
