package com.example.reservation.service;

import com.example.reservation.dto.request.ReservationCreateServiceRequest;
import com.example.db.entity.ScreeningSeat;
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

    public void validate(ReservationCreateServiceRequest request){

        // 1. 해당 상영 시간표에서 이미 예약한 좌석 수 확인
        if (request.getSeatIds() == null || request.getSeatIds().isEmpty() ||
                request.getSeatIds().stream().distinct().count() != request.getSeatIds().size()) {
            throw new ReservationException(INVALID_SEAT);
        }
        int alreadyReservedCount = reservationRepository.countByUserIdAndScreeningId(request.getUserId(), request.getScreeningId());

        // 2. 좌석이 모두 유효한지 확인
        List<ScreeningSeat> requestedSeats = screeningSeatRepository.findAllById(request.getSeatIds());
        if (requestedSeats.size() != request.getSeatIds().size()) {
            throw new ReservationException(INVALID_SEAT);
        }

        ReservationPolicy.validateSeats(requestedSeats, alreadyReservedCount);
    }
}
