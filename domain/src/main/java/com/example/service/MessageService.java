package com.example.service;

import org.springframework.stereotype.Service;

@Service
public class MessageService {

    public void send(String message) {
        try {
            Thread.sleep(500); // 메시지 발송 시간 시뮬레이션
            System.out.println("[FCM 메시지 전송됨] " + message);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
