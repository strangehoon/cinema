package com.example.reservation.service;

import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.core.LockConfiguration;
import net.javacrumbs.shedlock.core.LockingTaskExecutor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Profile;

import java.time.Duration;
import java.time.Instant;
import java.time.Clock;

@Component
@Profile("!test")
@RequiredArgsConstructor
public class BookingExpiryJob {
    private final LockingTaskExecutor locks;
    private final BookingService bookings;
    private final Clock clock;

    @Scheduled(fixedDelay = 10_000)
    public void run() {
        locks.executeWithLock((Runnable) bookings::expireDue, new LockConfiguration(
                Instant.now(clock), "expire-bookings", Duration.ofMinutes(2), Duration.ZERO));
    }
}
