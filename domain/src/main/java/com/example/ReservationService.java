package com.example;

import com.example.dto.request.ReservationServiceRequest;
import com.example.dto.response.ReservationServiceResponse;
import com.example.entity.User;
import com.example.event.ReservationCompletedEvent;
import com.example.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationService {

    private final ReservationValidator reservationValidator;
    private final ReservationLockHandler reservationLockHandler;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ReservationServiceResponse reserveSeats(ReservationServiceRequest request){

        reservationValidator.validate(request);

        User user = userRepository.findById(request.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        reservationLockHandler.handle(request, user);

        eventPublisher.publishEvent(ReservationCompletedEvent.of(user.getName(), request.getSeatIds().size()));

        return ReservationServiceResponse.builder()
                .userId(user.getId())
                .screeningId(request.getScreeningId())
                .reservedSeatIds(request.getSeatIds())
                .build();
    }
}