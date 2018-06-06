package com.izj.knowledge.service.base.aop;

import org.apache.commons.lang3.time.StopWatch;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import lombok.extern.slf4j.Slf4j;

@Aspect
@Slf4j
public class RepositoryLoggingInterceptor {

    @Around("execution(* com.izj.knowledge.service..repo.*Repository*.*(..))")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        StopWatch sw = new StopWatch();
        sw.start();
        try {
            return pjp.proceed();
        } catch (Throwable e) {
            throw e;
        } finally {
            sw.stop();
            log.debug("{}#{} elapsed time = {}ms",
                    pjp.getTarget().getClass().getName(),
                    pjp.getSignature().getName(),
                    sw.getTime());
        }
    }
}
