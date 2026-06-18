package com.example.reservation.service;

import com.example.db.entity.*;
import com.example.db.enums.PaymentStatus;
import com.example.db.repository.PaymentRepository;
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
import static com.example.screening.exception.ScreeningErrorCode.SCREENING_NOT_FOUND;
import static com.example.user.exception.UserErrorCode.USER_NOT_FOUND;

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

    public ReservationCreateServiceResponse createReserve(ReservationCreateServiceRequest request){

        reservationValidator.validate(request);

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new UserException(USER_NOT_FOUND));

        reservationLockHandler.handleWithAspectLock(request, user);

        List<Reservation> reservationsToUpdate = reservationRepository
                .findByScreeningIdAndScreeningSeatId(request.getScreeningId(), request.getSeatIds());

        Screening screening = screeningRepository.findById(request.getScreeningId())
                .orElseThrow(() -> new ScreeningException(SCREENING_NOT_FOUND));

        String orderId = UUID.randomUUID().toString();
        String orderName = screening.getMovie().getTitle() + " " + reservationsToUpdate.size() + "매";
        Long totalAmount = reservationsToUpdate.stream().mapToLong(Reservation::getPrice).sum();

        Payment payment = paymentRepository.save(Payment.of(orderId, orderName, totalAmount, PaymentStatus.READY, user));
        reservationsToUpdate.forEach(reservation -> reservation.putPayment(payment));

        return ReservationCreateServiceResponse.from(payment);
    }
}