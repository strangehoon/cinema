package com.example.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import redis.embedded.RedisServer;
import java.io.IOException;
import java.net.Socket;

@TestConfiguration
public class EmbeddedRedisConfig {

    @Value("${spring.redis.port}")
    private int port;

    private RedisServer redisServer;

    @PostConstruct
    public void startRedis() throws IOException {
        if (!isRedisRunning()) {
            redisServer = new RedisServer(port);
            redisServer.start();
        }
    }

    @PreDestroy
    public void stopRedis() {
        if (redisServer != null && redisServer.isActive()) {
            redisServer.stop();
        }
    }

    private boolean isRedisRunning() {
        try (Socket socket = new Socket("localhost", port)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @Bean(destroyMethod = "shutdown")
    @Primary
    public RedissonClient redissonClient() throws IOException {
        if (!isRedisRunning()) {
            startRedis();
        }

        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://localhost:" + port);

        return Redisson.create(config);
    }
}
