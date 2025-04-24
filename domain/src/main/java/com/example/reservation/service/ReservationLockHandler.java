package com.example.reservation.service;

import com.example.annotation.DistributedMultiLock;
import com.example.config.redis.lock.LockTemplate;
import com.example.reservation.dto.request.ReservationServiceRequest;
import com.example.entity.Reservation;
import com.example.entity.User;
import com.example.repository.ReservationRepository;
import com.example.reservation.exception.ReservationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;
import static com.example.reservation.exception.ReservationErrorCode.ALREADY_RESERVED_SEAT;
import static com.example.reservation.exception.ReservationErrorCode.RESERVATION_DATA_INCOMPLETE;

@Component
@RequiredArgsConstructor
public class ReservationLockHandler {

    private final ReservationRepository reservationRepository;
    private final LockTemplate lockTemplate;

    @DistributedMultiLock(expression = "#request.toLockKeys()")
    public void handleWithAspectLock(ReservationServiceRequest request, User user) {
        List<Reservation> reservationsToUpdate = reservationRepository
                .findByScreeningIdAndScreeningSeatIdInWithLock(request.getScreeningId(), request.getSeatIds());

        if (reservationsToUpdate.size() != request.getSeatIds().size()) {
            throw new ReservationException(RESERVATION_DATA_INCOMPLETE);
        }

        if (reservationsToUpdate.stream().anyMatch(Reservation::isReserved)) {
            throw new ReservationException(ALREADY_RESERVED_SEAT);
        }

        reservationsToUpdate.forEach(reservation -> reservation.reserve(user));
    }

    public void handleWithTemplateLock(ReservationServiceRequest request, User user) {
        List<String> lockKeys = request.toLockKeys(); // ex) ["1:5", "1:6", "1:7"]

        lockTemplate.executeMultiLock(lockKeys, () -> {
            List<Reservation> reservationsToUpdate = reservationRepository
                    .findByScreeningIdAndScreeningSeatIdInWithLock(request.getScreeningId(), request.getSeatIds());

            if (reservationsToUpdate.size() != request.getSeatIds().size()) {
                throw new ReservationException(RESERVATION_DATA_INCOMPLETE);
            }

            if (reservationsToUpdate.stream().anyMatch(Reservation::isReserved)) {
                throw new ReservationException(ALREADY_RESERVED_SEAT);
            }

            reservationsToUpdate.forEach(reservation -> reservation.reserve(user));
        });
    }
}