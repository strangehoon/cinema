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
import java.time.OffsetDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "payments")
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 40)
    private String paymentKey;

    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    private PaymentType type;

    @Column(length = 100)
    private String orderId;

    @Column(length = 50)
    private String orderName;

    private Long totalAmount;

    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    private PaymentMethod method;

    private LocalDateTime requestedAt;

    private LocalDateTime approvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private User user;

    @Builder
    public Payment(String paymentKey, PaymentType type, String orderId, String orderName, Long totalAmount,
                   PaymentStatus status, PaymentMethod method, LocalDateTime requestedAt,
                   LocalDateTime approvedAt, User user) {
        this.paymentKey = paymentKey;
        this.type = type;
        this.orderId = orderId;
        this.orderName = orderName;
        this.totalAmount = totalAmount;
        this.status = status;
        this.requestedAt = requestedAt;
        this.approvedAt = approvedAt;
    }

    public void update(String paymentKey, String type, String method, String status,
                       String requestedAt, String approvedAt){
        this.paymentKey = paymentKey;
        this.type = PaymentType.valueOf(type);
        this.method = PaymentMethod.fromValue(method);
        this.status = PaymentStatus.valueOf(status);
        this.requestedAt = OffsetDateTime.parse(requestedAt).toLocalDateTime();;
        this.approvedAt = OffsetDateTime.parse(approvedAt).toLocalDateTime();;
    }

    public static Payment of(String paymentKey, PaymentType type, String orderId, String orderName, Long totalAmount,
            PaymentStatus status, PaymentMethod method, LocalDateTime requestedAt, LocalDateTime approvedAt, User user) {

        return Payment.builder()
                .paymentKey(paymentKey)
                .type(type)
                .orderId(orderId)
                .orderName(orderName)
                .totalAmount(totalAmount)
                .status(status)
                .requestedAt(requestedAt)
                .approvedAt(approvedAt)
                .build();
    }

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