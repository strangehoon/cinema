package com.example;

import com.example.dto.request.ReservationServiceRequest;
import com.example.entity.Reservation;
import com.example.entity.ScreeningSeat;
import com.example.repository.ReservationRepository;
import com.example.repository.ScreeningSeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ReservationValidator {

    private final ReservationRepository reservationRepository;
    private final ScreeningSeatRepository screeningSeatRepository;

    public void validate(ReservationServiceRequest request){

        // 1. 해당 상영 시간표에서 이미 예약한 좌석 수 확인
        int alreadyReservedCount = reservationRepository.countByUserIdAndScreeningId(request.getMemberId(), request.getScreeningId());
        int requestedCount = request.getSeatIds().size();

        if (alreadyReservedCount + requestedCount > 5) {
            throw new IllegalArgumentException("1인당 최대 5좌석까지만 예매할 수 있습니다.");
        }

        // 2. 좌석이 모두 유효한지 확인
        List<ScreeningSeat> requestedSeats = screeningSeatRepository.findAllById(request.getSeatIds());
        if (requestedSeats.size() != request.getSeatIds().size()) {
            throw new IllegalArgumentException("유효하지 않은 좌석이 포함되어 있습니다.");
        }

        // 3. 좌석이 모두 같은 row인지 확인
        int row = requestedSeats.get(0).getRow();
        boolean sameRow = requestedSeats.stream().allMatch(seat -> seat.getRow() == row);
        if (!sameRow) {
            throw new IllegalArgumentException("좌석은 같은 행(row)이어야 합니다.");
        }

        // 4. 좌석이 인접한지(col 연속) 확인
        List<Integer> cols = requestedSeats.stream()
                .map(ScreeningSeat::getCol)
                .sorted()
                .collect(Collectors.toList());

        for (int i = 1; i < cols.size(); i++) {
            if (cols.get(i) != cols.get(i - 1) + 1) {
                throw new IllegalArgumentException("좌석은 같은 행에서 인접해야 합니다.");
            }
        }

        // 5. 해당 좌석이 이미 예약되었는지 확인
        List<Reservation> existingReservations = reservationRepository
                .findByScreeningIdAndScreeningSeatIdIn(request.getScreeningId(), request.getSeatIds());

        if (!existingReservations.isEmpty()) {
            throw new IllegalArgumentException("이미 예약된 좌석이 포함되어 있습니다.");
        }
    }
}
