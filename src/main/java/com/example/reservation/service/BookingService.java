package com.example.reservation.service;

import com.example.db.entity.BookingSeat;
import com.example.db.entity.Payment;
import com.example.db.entity.Reservation;
import com.example.db.enums.PaymentStatus;
import com.example.db.enums.ReservationStatus;
import com.example.db.repository.BookingSeatRepository;
import com.example.db.repository.PaymentRepository;
import com.example.db.repository.ReservationRepository;
import com.example.reservation.event.ReservationCompletedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {
    private final PaymentRepository payments;
    private final ReservationRepository reservations;
    private final BookingSeatRepository bookingSeats;
    private final ApplicationEventPublisher events;
    private final Clock clock;

    public record OrderView(String orderId, String name, long amount, String status,
                            LocalDateTime expiresAt, LocalDateTime startsAt, List<String> seats) {}
    public record PendingToss(String orderId, Long userId, String paymentKey, long amount) {}

    @Transactional
    public OrderView completeMock(String orderId, Long userId) {
        Payment payment = ownedPayment(orderId, userId);
        if (payment.getStatus() == PaymentStatus.DONE) return view(payment);
        if (payment.getStatus() != PaymentStatus.READY ||
                (payment.getExpiresAt() != null && !payment.getExpiresAt().isAfter(LocalDateTime.now(clock)))) {
            throw new IllegalArgumentException("결제 가능한 주문이 아닙니다.");
        }
        completeReservations(payment);
        return view(payment);
    }

    @Transactional(readOnly = true)
    public void failMock(String orderId, Long userId) {
        Payment payment = ownedPayment(orderId, userId);
        if (payment.getStatus() != PaymentStatus.READY || payment.getExpiresAt() == null ||
                !payment.getExpiresAt().isAfter(LocalDateTime.now(clock))) {
            throw new IllegalArgumentException("결제 가능한 주문이 아닙니다.");
        }
        throw new IllegalStateException("모의 결제가 실패했습니다. 보유 시간이 남아 있으면 다시 시도할 수 있습니다.");
    }

    @Transactional
    public OrderView startToss(String orderId, Long userId, String paymentKey) {
        Payment payment = ownedPayment(orderId, userId);
        if (payment.getStatus() == PaymentStatus.IN_PROGRESS && paymentKey.equals(payment.getPaymentKey())) {
            return view(payment);
        }
        if (payment.getStatus() != PaymentStatus.READY || payment.getExpiresAt() == null ||
                !payment.getExpiresAt().isAfter(LocalDateTime.now(clock))) {
            throw new IllegalArgumentException("결제 가능한 주문이 아닙니다.");
        }
        payment.setPaymentKey(paymentKey);
        payment.setStatus(PaymentStatus.IN_PROGRESS);
        return view(payment);
    }

    @Transactional
    public OrderView finishToss(String orderId, Long userId, String paymentKey) {
        Payment payment = ownedPayment(orderId, userId);
        if (payment.getStatus() == PaymentStatus.DONE && paymentKey.equals(payment.getPaymentKey())) return view(payment);
        if (payment.getStatus() != PaymentStatus.IN_PROGRESS || !paymentKey.equals(payment.getPaymentKey())) {
            throw new IllegalArgumentException("결제 가능한 주문이 아닙니다.");
        }
        completeReservations(payment);
        return view(payment);
    }

    @Transactional(readOnly = true)
    public String paymentKey(String orderId, Long userId) {
        return ownedPayment(orderId, userId).getPaymentKey();
    }

    @Transactional(readOnly = true)
    public List<PendingToss> pendingToss() {
        return payments.findByStatus(PaymentStatus.IN_PROGRESS).stream()
                .filter(payment -> payment.getPaymentKey() != null && payment.getUser() != null)
                .map(payment -> new PendingToss(payment.getOrderId(), payment.getUser().getId(),
                        payment.getPaymentKey(), payment.getTotalAmount()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PendingToss> pendingRefunds() {
        return payments.findByStatus(PaymentStatus.REFUNDING).stream()
                .filter(payment -> payment.getPaymentKey() != null && payment.getUser() != null)
                .map(payment -> new PendingToss(payment.getOrderId(), payment.getUser().getId(),
                        payment.getPaymentKey(), payment.getTotalAmount()))
                .toList();
    }

    @Transactional
    public OrderView beginTossRefund(String orderId, Long userId) {
        Payment payment = ownedPayment(orderId, userId);
        if (payment.getStatus() == PaymentStatus.REFUNDING) return view(payment);
        if (payment.getStatus() != PaymentStatus.DONE || payment.getPaymentKey() == null) {
            throw new IllegalArgumentException("환불 가능한 결제가 아닙니다.");
        }
        requireNotStarted(payment);
        payment.setStatus(PaymentStatus.REFUNDING);
        return view(payment);
    }

    @Transactional
    public OrderView failToss(String orderId, Long userId) {
        Payment payment = ownedPayment(orderId, userId);
        if (payment.getStatus() == PaymentStatus.CANCELED) return view(payment);
        if (payment.getStatus() != PaymentStatus.IN_PROGRESS) {
            throw new IllegalArgumentException("결제 확인 중인 주문이 아닙니다.");
        }
        payment.setStatus(PaymentStatus.CANCELED);
        releaseReservations(payment);
        return view(payment);
    }

    @Transactional
    public OrderView resetToss(String orderId, Long userId) {
        Payment payment = ownedPayment(orderId, userId);
        if (payment.getStatus() != PaymentStatus.IN_PROGRESS) return view(payment);
        payment.setPaymentKey(null);
        if (payment.getExpiresAt() != null && payment.getExpiresAt().isAfter(LocalDateTime.now(clock))) {
            payment.setStatus(PaymentStatus.READY);
        } else {
            payment.setStatus(PaymentStatus.EXPIRED);
            releaseReservations(payment);
        }
        return view(payment);
    }

    @Transactional
    public OrderView cancel(String orderId, Long userId) {
        Payment payment = ownedPayment(orderId, userId);
        if (payment.getStatus() == PaymentStatus.CANCELED) return view(payment);
        if (payment.getStatus() == PaymentStatus.READY) {
            payment.setStatus(PaymentStatus.CANCELED);
            releaseReservations(payment);
            return view(payment);
        }
        if (payment.getStatus() != PaymentStatus.DONE && payment.getStatus() != PaymentStatus.REFUNDING)
            throw new IllegalArgumentException("결제 완료된 주문만 취소할 수 있습니다.");
        if (payment.getStatus() == PaymentStatus.DONE) requireNotStarted(payment);
        payment.setStatus(PaymentStatus.CANCELED);
        releaseReservations(payment);
        return view(payment);
    }

    @Transactional(readOnly = true)
    public List<OrderView> orders(Long userId) {
        return payments.findByUserIdOrderByIdDesc(userId).stream().map(this::view).toList();
    }

    @Transactional(readOnly = true)
    public OrderView order(String orderId, Long userId) { return view(ownedPayment(orderId, userId)); }

    @Transactional
    public void expireDue() {
        payments.findByStatusAndExpiresAtBefore(PaymentStatus.READY, LocalDateTime.now(clock)).forEach(payment -> {
            payment.setStatus(PaymentStatus.EXPIRED);
            releaseReservations(payment);
        });
    }

    private void completeReservations(Payment payment) {
        payment.setStatus(PaymentStatus.DONE);
        List<Reservation> booked = reservations.findByPaymentId(payment.getId());
        booked.forEach(reservation -> reservation.updateStatus(ReservationStatus.COMPLETED));
        events.publishEvent(ReservationCompletedEvent.of(payment.getUser().getName(), booked.size()));
    }

    private void releaseReservations(Payment payment) {
        reservations.findByPaymentId(payment.getId()).forEach(Reservation::release);
    }

    private void requireNotStarted(Payment payment) {
        if (bookingSeats.findByOrderIdOrderById(payment.getOrder().getId()).stream()
                .anyMatch(seat -> !seat.getStartsAt().isAfter(LocalDateTime.now(clock)))) {
            throw new IllegalArgumentException("상영이 시작된 예매는 취소할 수 없습니다.");
        }
    }

    private Payment ownedPayment(String orderId, Long userId) {
        Payment payment = payments.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));
        if (payment.getUser() == null || !payment.getUser().getId().equals(userId)) {
            throw new SecurityException("이 주문에 접근할 수 없습니다.");
        }
        return payment;
    }

    private OrderView view(Payment payment) {
        List<BookingSeat> seats = bookingSeats.findByOrderIdOrderById(payment.getOrder().getId());
        return new OrderView(payment.getOrderId(), payment.getOrderName(), payment.getTotalAmount(),
                payment.getStatus().name(), payment.getExpiresAt(),
                seats.isEmpty() ? null : seats.getFirst().getStartsAt(),
                seats.stream().map(BookingSeat::getSeatLabel).toList());
    }
}
