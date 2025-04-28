package com.example.payment.service;

import com.example.db.entity.Payment;
import com.example.db.entity.Reservation;
import com.example.db.enums.ReservationStatus;
import com.example.db.repository.PaymentRepository;
import com.example.db.repository.ReservationRepository;
import com.example.event.dto.ReservationCompletedEvent;
import com.example.payment.dto.request.CompletePaymentServiceRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final ReservationRepository reservationRepository;

    public void completePayment(CompletePaymentServiceRequest request) {

        Payment payment = paymentRepository.findByOrderId(request.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 결제 정보입니다."));

        payment.update(request.getPaymentKey(), request.getType(), request.getMethod(), request.getStatus(),
                request.getRequestedAt(), request.getApprovedAt());

        List<Reservation> reservations = reservationRepository.findByPaymentId(payment.getId())
                .orElseThrow(() -> new IllegalArgumentException("결제에 연결된 예약 정보가 없습니다."));

        reservations.forEach(reservation -> reservation.updateStatus(ReservationStatus.COMPLETED));
        eventPublisher.publishEvent(ReservationCompletedEvent.of("테스트 사용자1", 100));
    }
}