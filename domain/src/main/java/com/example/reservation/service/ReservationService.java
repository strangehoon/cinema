package com.example.reservation.service;

import com.example.db.entity.*;
import com.example.db.enums.PaymentStatus;
import com.example.db.repository.PaymentRepository;
import com.example.db.repository.ReservationRepository;
import com.example.db.repository.ScreeningRepository;
import com.example.reservation.dto.request.ReservationServiceRequest;
import com.example.reservation.dto.response.ReservationServiceResponse;
import com.example.db.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationService {

    private final ReservationValidator reservationValidator;
    private final ReservationLockHandler reservationLockHandler;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final ScreeningRepository screeningRepository;

    public ReservationServiceResponse reserveSeats(ReservationServiceRequest request){

        reservationValidator.validate(request);

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        reservationLockHandler.handleWithAspectLock(request, user);

        List<Reservation> reservations = reservationRepository
                .findByScreeningIdAndScreeningSeatIdInWithLock(request.getScreeningId(), request.getSeatIds());

        Screening screening = screeningRepository.findById(request.getScreeningId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상영 정보입니다."));

        String orderId = UUID.randomUUID().toString();
        String orderName = screening.getMovie().getTitle() + " " + reservations.size() + "매";
        Long totalAmount = reservations.stream().mapToLong(Reservation::getPrice).sum();

        Payment payment = paymentRepository.save(Payment.of(orderId, orderName, totalAmount, PaymentStatus.READY, user));
        reservations.forEach(reservation -> reservation.putPayment(payment));

        return ReservationServiceResponse.from(payment);
    }
}