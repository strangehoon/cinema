package com.example.common.lock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.RedissonMultiLock;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import java.lang.reflect.Method;
import java.util.List;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class DistributedMultiLockAspect {

    private final RedissonClient redissonClient;
    private final ExpressionParser parser = new SpelExpressionParser();
    private final ParameterNameDiscoverer nameDiscoverer = new DefaultParameterNameDiscoverer();
    private final AopForTransaction aopForTransaction;

    @Around("@annotation(distributedMultiLock)")
    public Object lock(ProceedingJoinPoint joinPoint, DistributedMultiLock distributedMultiLock) throws Throwable {
        List<String> keys = getLockKeys(joinPoint, distributedMultiLock.expression());

        List<RLock> locks = keys.stream().map(redissonClient::getLock).toList();
        RLock multiLock = new RedissonMultiLock(locks.toArray(new RLock[0]));

        boolean acquired = multiLock.tryLock(
                distributedMultiLock.waitTime(),
                distributedMultiLock.leaseTime(),
                distributedMultiLock.timeUnit()
        );

        if (!acquired) {
            throw new IllegalStateException("좌석 락 획득 실패: " + keys);
        }

        log.info("스레드: {}, 락 키: {}", Thread.currentThread().getName(), keys);

        try {
            return aopForTransaction.proceed(joinPoint);
        } catch (Exception e){
            throw e;
        }
        finally {
            try {
                multiLock.unlock();
            } catch (Exception e) {
                log.error("락 해제 중 예외 발생", e);

            }
        }
    }

    private List<String> getLockKeys(ProceedingJoinPoint joinPoint, String expression) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();

        EvaluationContext context = new StandardEvaluationContext();
        String[] paramNames = nameDiscoverer.getParameterNames(method);
        Object[] args = joinPoint.getArgs();

        if (paramNames != null) {
            for (int i = 0; i < paramNames.length; i++) {
                context.setVariable(paramNames[i], args[i]);
            }
        }

        List<String> evaluatedKeys = parser.parseExpression(expression).getValue(context, List.class);

        if (evaluatedKeys == null || evaluatedKeys.isEmpty()) {
            throw new IllegalStateException("분산락 키 생성 실패: expression=" + expression);
        }
        return evaluatedKeys.stream()
                .map(k -> "lock:" + k)
                .sorted()
                .toList();
    }
}