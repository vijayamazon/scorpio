package com.onlymaker.scorpio.mws;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class ServiceAspect {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceAspect.class);

    @Pointcut("execution(* com.onlymaker.scorpio.mws.*Service.*(..))")
    private void servicePointCut() {
    }

    @Before("servicePointCut()")
    private void before(JoinPoint joinPoint) {
        LOGGER.info("{}->{}({})", joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName(), Arrays.toString(joinPoint.getArgs()));
    }
}
