package com.example.reservation.service;

import com.example.common.lock.DistributedMultiLock;
import com.example.db.enums.ReservationStatus;
import com.example.reservation.dto.request.ReservationCreateServiceRequest;
import com.example.db.entity.Reservation;
import com.example.db.entity.User;
import com.example.db.repository.ReservationRepository;
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

    @DistributedMultiLock(expression = "#request.toLockKeys()")
    public void handleWithAspectLock(ReservationCreateServiceRequest request, User user) {
        List<Reservation> reservationsToUpdate = reservationRepository
                .findByScreeningIdAndScreeningSeatId(request.getScreeningId(), request.getSeatIds());

        if (reservationsToUpdate.size() != request.getSeatIds().size()) {
            throw new ReservationException(RESERVATION_DATA_INCOMPLETE);
        }

        if (reservationsToUpdate.stream().anyMatch(reservation -> reservation.getStatus() != ReservationStatus.NONE)) {
            throw new ReservationException(ALREADY_RESERVED_SEAT);
        }

        reservationsToUpdate.forEach(reservation -> reservation.reserve(user));
    }
}