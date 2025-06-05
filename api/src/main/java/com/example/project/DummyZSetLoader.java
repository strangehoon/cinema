package com.example.project;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
@RequiredArgsConstructor
public class DummyZSetLoader implements CommandLineRunner {

    private final RedisTemplate<String, Object> redisTemplate;
    private final Random random = new Random();

    @Override
    public void run(String... args) {
        ZSetOperations<String, Object> zSetOps = redisTemplate.opsForZSet();

        for (int i = 1; i <= 1000000; i++) {
            String projectId = "project_" + i;
            double score = random.nextInt(100);
            zSetOps.add("project:likes", projectId, score);
        }
    }
}
