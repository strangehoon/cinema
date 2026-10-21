package com.example.db.config;

import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.core.LockConfiguration;
import net.javacrumbs.shedlock.core.LockingTaskExecutor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.Clock;

@Component
@Profile("!test")
@RequiredArgsConstructor
public class SampleDataSeeder implements ApplicationRunner {
    private final LockingTaskExecutor locks;
    private final SampleDataWriter writer;
    private final Clock clock;

    @Override
    public void run(ApplicationArguments args) {
        seedOnce();
    }

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void refreshSchedules() {
        seedOnce();
    }

    private void seedOnce() {
        locks.executeWithLock((Runnable) writer::seed, new LockConfiguration(
                Instant.now(clock), "sample-data", Duration.ofMinutes(10), Duration.ZERO));
    }
}
