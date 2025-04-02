package com.example;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReservationEventHandler {

    private final MessageService messageService;

    @Async
    public void sendReservationCompleteMessage(Long userId, int seatCount) {
        String message = "회원 " + userId + "님, 좌석 " + seatCount + "개 예약이 완료되었습니다.";
        messageService.send(message);
    }
}
