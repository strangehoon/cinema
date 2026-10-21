package com.example.reservation.service;

import com.example.reservation.dto.request.ReservationCreateServiceRequest;
import com.example.db.entity.ScreeningSeat;
import com.example.db.entity.Screening;
import com.example.db.entity.Reservation;
import com.example.db.enums.ReservationStatus;
import com.example.db.repository.ReservationRepository;
import com.example.db.repository.ScreeningSeatRepository;
import com.example.reservation.exception.ReservationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;
import static com.example.reservation.exception.ReservationErrorCode.*;

@Component
@RequiredArgsConstructor
public class ReservationValidator {

    private final ReservationRepository reservationRepository;
    private final ScreeningSeatRepository screeningSeatRepository;

    public void validateSeatIds(List<Long> seatIds) {
        if (seatIds == null || seatIds.isEmpty() || seatIds.stream().distinct().count() != seatIds.size()) {
            throw new ReservationException(INVALID_SEAT);
        }
    }

    public void validate(ReservationCreateServiceRequest request){
        validateSeatIds(request.getSeatIds());
        int alreadyReservedCount = reservationRepository.countByUserIdAndScreeningId(request.getUserId(), request.getScreeningId());
        List<ScreeningSeat> requestedSeats = screeningSeatRepository.findAllById(request.getSeatIds());
        if (requestedSeats.size() != request.getSeatIds().size()) {
            throw new ReservationException(INVALID_SEAT);
        }

        ReservationPolicy.validateSeats(requestedSeats, alreadyReservedCount);
    }

    public void validateInventory(Screening screening, List<Reservation> inventory, List<Long> seatIds) {
        if (inventory.size() != seatIds.size()) {
            throw new ReservationException(RESERVATION_DATA_INCOMPLETE);
        }
        if (inventory.stream().anyMatch(reservation ->
                !reservation.getScreeningSeat().getTheater().getId().equals(screening.getTheater().getId()))) {
            throw new ReservationException(INVALID_SEAT);
        }
        if (inventory.stream().anyMatch(reservation -> reservation.getStatus() != ReservationStatus.NONE)) {
            throw new ReservationException(ALREADY_RESERVED_SEAT);
        }
    }
}
