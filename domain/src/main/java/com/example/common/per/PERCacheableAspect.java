package com.example.common.per;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import java.lang.reflect.Method;
import java.util.List;

@Slf4j
@Aspect
@Order(-1)
@Component
@RequiredArgsConstructor
public class PERCacheableAspect {

    private static final ExpressionParser PARSER = new SpelExpressionParser();
    private static final DefaultParameterNameDiscoverer NAME_DISCOVERER = new DefaultParameterNameDiscoverer();
    private final RedisTemplate<String, String> redisTemplate;
    private final DefaultRedisScript<List> getScript;
    private final DefaultRedisScript<Long> setScript;
    private final ObjectMapper objectMapper;
    private static final double BETA = 1;

    @Around("@annotation(perCacheable)")
    public Object handlePERCache(ProceedingJoinPoint joinPoint, PERCacheable perCacheable) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Object[] args = joinPoint.getArgs();

        StandardEvaluationContext context = new MethodBasedEvaluationContext(null, method, args, NAME_DISCOVERER);

        if (!evaluateCondition(perCacheable.condition(), context)) {
            return joinPoint.proceed();
        }

        String key = PARSER.parseExpression(perCacheable.key()).getValue(context, String.class);
        int ttlMillis = perCacheable.ttl() * 1000;
        String deltaKey = key + ":delta";

        List<Object> result = redisTemplate.execute(getScript, List.of(key, deltaKey));
        Object cachedObject = result.get(0);
        Object deltaObject = result.get(1);
        Long ttl = ((Number) result.get(2)).longValue();
        Long delta = deltaObject == null ? null : Long.parseLong(deltaObject.toString());

        double x = Math.log(Math.random());
        if (cachedObject == null || delta == null || ttl == null || -1 * delta * BETA * x >= ttl) {
            long start = System.currentTimeMillis();
            Object recomputed;
            try {
                recomputed = joinPoint.proceed();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }

            long recomputationTime = System.currentTimeMillis() - start;
            List<String> keys = List.of(key, deltaKey);
            String json = objectMapper.writeValueAsString(recomputed);
            String deltaString = String.valueOf(recomputationTime);
            String ttlString = String.valueOf(ttlMillis);

            redisTemplate.execute(setScript, keys, json, deltaString, ttlString);
            return recomputed;
        }

        return objectMapper.readValue(cachedObject.toString(), createTypeReference(method));
    }

    private boolean evaluateCondition(String condition, StandardEvaluationContext context) {
        Boolean result = PARSER.parseExpression(condition).getValue(context, Boolean.class);
        return result != null && result;
    }

    private TypeReference<Object> createTypeReference(Method method) {
        java.lang.reflect.Type returnType = method.getGenericReturnType();
        return new TypeReference<>() {
            @Override
            public java.lang.reflect.Type getType() {
                return returnType;
            }
        };
    }
}