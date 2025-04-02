package com.example;

import com.example.dto.request.ReservationServiceRequest;
import com.example.entity.Reservation;
import com.example.entity.Screening;
import com.example.entity.ScreeningSeat;
import com.example.entity.User;
import com.example.repository.ReservationRepository;
import com.example.repository.ScreeningRepository;
import com.example.repository.ScreeningSeatRepository;
import com.example.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationService {

    private final ReservationValidator reservationValidator;
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final ScreeningRepository screeningRepository;
    private final ScreeningSeatRepository screeningSeatRepository;
    private final ReservationEventHandler reservationEventHandler;

    public String reserveSeats(ReservationServiceRequest request){

        reservationValidator.validate(request);

        User user = userRepository.findById(request.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));
        Screening screening = screeningRepository.findById(request.getScreeningId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상영 시간표입니다."));

        List<ScreeningSeat> requestedSeats = screeningSeatRepository.findAllById(request.getSeatIds());

        List<Reservation> newReservations = requestedSeats.stream()
                .map(seat -> Reservation.builder()
                        .screeningSeat(seat)
                        .screening(screening)
                        .user(user)
                        .build())
                .collect(Collectors.toList());

        reservationRepository.saveAll(newReservations);
        reservationEventHandler.sendReservationCompleteMessage(user.getId(), newReservations.size());
        
        return "총 " + newReservations.size() + "개의 좌석이 예약되었습니다.";
    }
}
