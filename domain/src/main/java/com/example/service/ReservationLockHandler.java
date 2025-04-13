package com.example.service;

import com.example.common.DistributedMultiLock;
import com.example.common.LockTemplate;
import com.example.dto.request.ReservationServiceRequest;
import com.example.entity.Reservation;
import com.example.entity.User;
import com.example.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;

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
            throw new IllegalStateException("요청한 좌석에 대한 Reservation 데이터가 일부 누락되었습니다.");
        }

        if (reservationsToUpdate.stream().anyMatch(Reservation::isReserved)) {
            throw new IllegalStateException("이미 예약된 좌석이 포함되어 있습니다.");
        }

        reservationsToUpdate.forEach(reservation -> reservation.reserve(user));
    }

    public void handleWithTemplateLock(ReservationServiceRequest request, User user) {
        List<String> lockKeys = request.toLockKeys(); // ex) ["1:5", "1:6", "1:7"]

        lockTemplate.executeMultiLock(lockKeys, () -> {
            List<Reservation> reservationsToUpdate = reservationRepository
                    .findByScreeningIdAndScreeningSeatIdInWithLock(request.getScreeningId(), request.getSeatIds());

            if (reservationsToUpdate.size() != request.getSeatIds().size()) {
                throw new IllegalStateException("요청한 좌석에 대한 Reservation 데이터가 일부 누락되었습니다.");
            }

            if (reservationsToUpdate.stream().anyMatch(Reservation::isReserved)) {
                throw new IllegalStateException("이미 예약된 좌석이 포함되어 있습니다.");
            }

            reservationsToUpdate.forEach(reservation -> reservation.reserve(user));
        });
    }
}