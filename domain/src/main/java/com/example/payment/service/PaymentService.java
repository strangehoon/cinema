package com.example.payment.service;

import com.example.db.entity.Payment;
import com.example.db.entity.Reservation;
import com.example.db.enums.ReservationStatus;
import com.example.db.repository.PaymentRepository;
import com.example.db.repository.ReservationRepository;
import com.example.reservation.event.ReservationCompletedEvent;
import com.example.payment.dto.request.PaymentCompleteServiceRequest;
import com.example.payment.exception.PaymentException;
import com.example.reservation.exception.ReservationException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import static com.example.payment.exception.PaymentErrorCode.PAYMENT_NOT_FOUND;
import static com.example.reservation.exception.ReservationErrorCode.RESERVATION_NOT_FOUND_BY_PAYMENT;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final ReservationRepository reservationRepository;

    public void completePayment(PaymentCompleteServiceRequest request) {

        Payment payment = paymentRepository.findByOrderId(request.getOrderId())
                .orElseThrow(() -> new PaymentException(PAYMENT_NOT_FOUND));

        payment.update(request.getPaymentKey(), request.getType(), request.getMethod(), request.getStatus(),
                request.getRequestedAt(), request.getApprovedAt());

        List<Reservation> reservations = reservationRepository.findByPaymentId(payment.getId());
        if (reservations.isEmpty()) {
            throw new ReservationException(RESERVATION_NOT_FOUND_BY_PAYMENT);
        }

        reservations.forEach(reservation -> reservation.updateStatus(ReservationStatus.COMPLETED));
        eventPublisher.publishEvent(ReservationCompletedEvent.of("테스트 사용자1", 100));
    }
}