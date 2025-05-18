package com.example.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
@Slf4j
public class PerCacheService<T> {

    private final RedisTemplate<String, String> redisTemplate;
    private final DefaultRedisScript<List> getScript;
    private final DefaultRedisScript<Long> setScript;
    private final ObjectMapper objectMapper;
    private static final double BETA = 0.1;

    public T get(String key, Supplier<T> recompute, TypeReference<T> typeRef, int ttlMillis) throws JsonProcessingException {
        String deltaKey = key + ":delta";
        List<Object> result = redisTemplate.execute(getScript, List.of(key, deltaKey));
        Object deltaObject = result.get(1);

        Object cachedObject = result.get(0);
        Long delta = deltaObject==null ? null : Long.parseLong(deltaObject.toString());
        Long ttl = ((Number) result.get(2)).longValue();

        if(cachedObject == null || delta == null || ttl == null || -1 * delta * BETA * Math.log(Math.random())>=ttl){
            long start = System.currentTimeMillis();
            T recomputed = recompute.get();
            long recomputationTime = System.currentTimeMillis() - start;
            List<String> keys = List.of(key, deltaKey);
            String json = objectMapper.writeValueAsString(recomputed);
            String deltaString = String.valueOf(recomputationTime);
            String ttlString = String.valueOf(ttlMillis);
            redisTemplate.execute(setScript, keys, json, deltaString, ttlString);
            return recomputed;
        }
        return objectMapper.readValue(cachedObject.toString(), typeRef);
    }
}
