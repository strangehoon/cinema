package com.example.reservation.service;

import com.example.db.entity.*;
import com.example.db.enums.PaymentStatus;
import com.example.db.repository.PaymentRepository;
import com.example.db.repository.BookingSeatRepository;
import com.example.db.repository.ReservationRepository;
import com.example.db.repository.ScreeningRepository;
import com.example.reservation.dto.request.ReservationCreateServiceRequest;
import com.example.reservation.dto.response.ReservationCreateServiceResponse;
import com.example.db.repository.UserRepository;
import com.example.screening.exception.ScreeningException;
import com.example.user.exception.UserException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;
import java.time.Clock;
import java.time.LocalDateTime;
import static com.example.screening.exception.ScreeningErrorCode.SCREENING_NOT_FOUND;
import static com.example.user.exception.UserErrorCode.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationService {

    private final ReservationValidator reservationValidator;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final ReservationRepository reservationRepository;
    private final ScreeningRepository screeningRepository;
    private final Clock clock;

    public ReservationCreateServiceResponse createReserve(ReservationCreateServiceRequest request){

        User user = userRepository.findByIdForUpdate(request.getUserId())
                .orElseThrow(() -> new UserException(USER_NOT_FOUND));
        Screening screening = screeningRepository.findById(request.getScreeningId())
                .orElseThrow(() -> new ScreeningException(SCREENING_NOT_FOUND));
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime expiresAt = ReservationPolicy.expiresAt(now, screening.getStartedAt());
        reservationValidator.validateSeatIds(request.getSeatIds());
        List<Long> seatIdsInLockOrder = request.getSeatIds().stream().sorted().toList();
        List<Reservation> reservationsToUpdate = reservationRepository
                .findByScreeningIdAndScreeningSeatIdForUpdate(request.getScreeningId(), seatIdsInLockOrder);
        // Scalar lookup avoids initializing a payment proxy created by a transaction we waited on.
        List<Long> expiredPaymentIds = reservationRepository.findExpiredPaymentIds(
                reservationsToUpdate.stream().map(Reservation::getId).toList(), PaymentStatus.READY, now);
        paymentRepository.findAllById(expiredPaymentIds).forEach(payment -> {
            payment.setStatus(PaymentStatus.EXPIRED);
            reservationRepository.findByPaymentId(payment.getId()).forEach(Reservation::release);
        });
        reservationValidator.validate(request);
        reservationValidator.validateInventory(screening, reservationsToUpdate, request.getSeatIds());
        reservationsToUpdate.forEach(reservation -> reservation.reserve(user));

        String orderId = UUID.randomUUID().toString();
        String orderName = screening.getMovie().getTitle() + " " + reservationsToUpdate.size() + "매";
        Long totalAmount = reservationsToUpdate.stream().mapToLong(Reservation::getPrice).sum();

        Payment payment = paymentRepository.save(Payment.of(orderId, orderName, totalAmount, PaymentStatus.READY, user));
        payment.setExpiresAt(expiresAt);
        reservationsToUpdate.forEach(reservation -> reservation.putPayment(payment));
        reservationsToUpdate.forEach(reservation -> bookingSeatRepository.save(new BookingSeat(payment.getOrder(),
                screening.getId(), screening.getStartedAt(),
                String.valueOf((char) ('A' + reservation.getScreeningSeat().getRow() - 1))
                        + reservation.getScreeningSeat().getCol(), reservation.getPrice())));

        return ReservationCreateServiceResponse.from(payment);
    }
}
