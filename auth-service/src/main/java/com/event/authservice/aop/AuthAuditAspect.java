package com.event.authservice.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AuthAuditAspect {

    private static final Logger log = LoggerFactory.getLogger(AuthAuditAspect.class);

    @Around("@annotation(auditAction)")
    public Object audit(ProceedingJoinPoint joinPoint, AuditAction auditAction) throws Throwable {
        String action = auditAction.value();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        long start = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            log.info("AUDIT action={} method={} outcome=SUCCESS durationMs={}",
                    action,
                    signature.toShortString(),
                    System.currentTimeMillis() - start);
            return result;
        } catch (Throwable ex) {
            log.warn("AUDIT action={} method={} outcome=FAILURE durationMs={} reason={}",
                    action,
                    signature.toShortString(),
                    System.currentTimeMillis() - start,
                    ex.getClass().getSimpleName());
            throw ex;
        }
    }
}
