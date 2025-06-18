package com.example.event.service;

import com.example.event.dto.ReservationCompletedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ReservationEventHandler {

    private final MessageService messageService;

    @Async
    @TransactionalEventListener
    public void handle(ReservationCompletedEvent event) {
        String message = "회원 %s님, 좌석 %d개 예약이 완료되었습니다."
                .formatted(event.getUserName(), event.getSeatCount());
        messageService.send(message);
    }
}
