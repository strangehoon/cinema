package com.example;

import com.example.dto.request.ReservationServiceRequest;
import com.example.dto.response.ReservationServiceResponse;
import com.example.entity.Reservation;
import com.example.entity.User;
import com.example.event.ReservationCompletedEvent;
import com.example.repository.ReservationRepository;
import com.example.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationService {

    private final ReservationValidator reservationValidator;
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ReservationServiceResponse reserveSeats(ReservationServiceRequest request){

        reservationValidator.validate(request);

        User user = userRepository.findById(request.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        List<Reservation> reservationsToUpdate = reservationRepository
                .findByScreeningIdAndScreeningSeatIdInWithLock(request.getScreeningId(), request.getSeatIds());

        boolean hasReserved = reservationsToUpdate.stream().anyMatch(Reservation::isReserved);

        if(reservationsToUpdate.size()!=request.getSeatIds().size()){
            throw new IllegalStateException("요청한 좌석에 대한 Reservation 데이터가 일부 누락되었습니다.");
        }

        if (hasReserved) {
            throw new IllegalStateException("이미 예약된 좌석이 포함되어 있습니다.");
        }

        reservationsToUpdate.forEach(reservation -> {
            reservation.reserve(user);
        });

        eventPublisher.publishEvent(ReservationCompletedEvent.of(user.getName(), reservationsToUpdate.size()));

        return ReservationServiceResponse.builder()
                .userId(user.getId())
                .screeningId(request.getScreeningId())
                .reservedSeatIds(request.getSeatIds())
                .build();
    }
}