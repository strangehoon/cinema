package com.example.common;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import java.lang.reflect.Method;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class PERCacheableAspect {

    private final PerCacheService<Object> perCacheService;
    private static final ExpressionParser PARSER = new SpelExpressionParser();
    private static final DefaultParameterNameDiscoverer NAME_DISCOVERER = new DefaultParameterNameDiscoverer();

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

        return perCacheService.get(
                key,
                () -> {
                    try {
                        return joinPoint.proceed();
                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                },
                createTypeReference(method),
                ttlMillis
        );
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