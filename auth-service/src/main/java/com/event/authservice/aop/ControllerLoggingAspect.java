package com.event.authservice.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ControllerLoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(ControllerLoggingAspect.class);

    @Around("within(com.event.authservice.controller..*)")
    public Object logControllerExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            log.info("Auth controller {} executed in {} ms",
                    joinPoint.getSignature().toShortString(),
                    System.currentTimeMillis() - start);
            return result;
        } catch (Throwable ex) {
            log.error("Auth controller {} failed after {} ms: {}",
                    joinPoint.getSignature().toShortString(),
                    System.currentTimeMillis() - start,
                    ex.getMessage());
            throw ex;
        }
    }
}
