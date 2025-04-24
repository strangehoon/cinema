package com.example.reservation.service;

import com.example.reservation.dto.request.ReservationServiceRequest;
import com.example.entity.ScreeningSeat;
import com.example.repository.ReservationRepository;
import com.example.repository.ScreeningSeatRepository;
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

    public void validate(ReservationServiceRequest request){

        // 1. 해당 상영 시간표에서 이미 예약한 좌석 수 확인
        int alreadyReservedCount = reservationRepository.countByUserIdAndScreeningId(request.getUserId(), request.getScreeningId());
        int requestedCount = request.getSeatIds().size();

        if (alreadyReservedCount + requestedCount > 5) {
            throw new ReservationException(MAX_SEAT_LIMIT_EXCEEDED);
        }

        // 2. 좌석이 모두 유효한지 확인
        List<ScreeningSeat> requestedSeats = screeningSeatRepository.findAllById(request.getSeatIds());
        if (requestedSeats.size() != request.getSeatIds().size()) {
            throw new ReservationException(INVALID_SEAT);
        }

        // 3. 좌석이 모두 같은 row인지 확인
        int row = requestedSeats.get(0).getRow();
        boolean sameRow = requestedSeats.stream().allMatch(seat -> seat.getRow() == row);
        if (!sameRow) {
            throw new ReservationException(NOT_SAME_ROW);
        }

        // 4. 좌석이 인접한지(col 연속) 확인
        List<Integer> cols = requestedSeats.stream()
                .map(ScreeningSeat::getCol)
                .sorted()
                .collect(Collectors.toList());

        for (int i = 1; i < cols.size(); i++) {
            if (cols.get(i) != cols.get(i - 1) + 1) {
                throw new ReservationException(NOT_ADJACENT_SEAT);
            }
        }
    }
}