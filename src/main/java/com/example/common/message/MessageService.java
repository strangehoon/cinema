package com.example.common.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MessageService {
    private static final Logger log = LoggerFactory.getLogger(MessageService.class);

    public void send(String message) {
        log.info("[로컬 예매 알림] {}", message);
    }
}
