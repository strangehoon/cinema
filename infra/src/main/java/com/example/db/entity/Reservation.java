package com.example.db.entity;

import com.example.db.enums.ReservationStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import static com.example.db.enums.ReservationStatus.IN_PROGRESS;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "reservations")
public class Reservation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    @Column
    private int price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "screening_seat_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private ScreeningSeat screeningSeat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "screening_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Screening screening;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Payment payment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private User user;

    @Builder
    private Reservation(ReservationStatus status,ScreeningSeat screeningSeat, Screening screening, User user) {
        this.status = status;
        this.screeningSeat = screeningSeat;
        this.screening = screening;

    }

    public void reserve(User user) {
        this.user = user;
        this.status = IN_PROGRESS;
    }

    public void updateStatus(ReservationStatus status) {
        this.status = status;
    }

    public void putPayment(Payment payment){
        this.payment = payment;
    }
}
