package com.example.config.redis.lock;

import lombok.RequiredArgsConstructor;
import org.redisson.RedissonMultiLock;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class LockTemplate {

    private final RedissonClient redissonClient;

    public void executeMultiLock(List<String> rawKeys, Runnable criticalSection) {
        List<RLock> locks = rawKeys.stream()
                .map(key -> redissonClient.getLock("lock:" + key)) // prefix 포함
                .sorted((a, b) -> a.getName().compareTo(b.getName())) // 데드락 방지
                .toList();

        RLock multiLock = new RedissonMultiLock(locks.toArray(new RLock[0]));

        boolean acquired = false;
        try {
            acquired = multiLock.tryLock(2, 3, TimeUnit.SECONDS); // waitTime, leaseTime
            if (!acquired) {
                throw new IllegalStateException("락 획득 실패: " + rawKeys);
            }

            criticalSection.run(); // 💡 핵심 로직 실행

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("락 획득 중 인터럽트 발생", e);
        } finally {
            if (acquired) {
                try {
                    multiLock.unlock();
                } catch (Exception e) {
                    System.err.println("락 해제 중 예외 발생: " + e.getMessage());
                }
            }
        }
    }
}
