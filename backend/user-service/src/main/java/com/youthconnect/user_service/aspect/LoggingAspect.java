package com.youthconnect.user_service.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.util.Arrays;

/**
 * Comprehensive Logging Aspect for Youth Connect Uganda User Service
 *
 * Features:
 * - Method entry/exit logging with parameters
 * - Execution time monitoring
 * - Exception logging with context
 * - Sensitive data masking for security
 * - Performance threshold alerts
 *
 * @author Douglas Kings Kato
 * @version 1.0.1 - FIXED package references
 */
@Slf4j
@Aspect
@Component
public class LoggingAspect {

    /**
     * Pointcut for all service layer methods
     * ✅ FIXED: Updated package name to com.youthconnect
     */
    @Pointcut("execution(* com.youthconnect.user_service.service..*(..))")
    public void serviceLayer() {}

    /**
     * Pointcut for all repository layer methods
     * ✅ FIXED: Updated package name to com.youthconnect
     */
    @Pointcut("execution(* com.youthconnect.user_service.repository..*(..))")
    public void repositoryLayer() {}

    /**
     * Pointcut for all controller layer methods
     * ✅ FIXED: Updated package name to com.youthconnect
     */
    @Pointcut("execution(* com.youthconnect.user_service.controller..*(..))")
    public void controllerLayer() {}

    /**
     * Around advice for comprehensive method logging
     * Logs method entry, execution time, and exit with masked sensitive data
     */
    @Around("serviceLayer() || repositoryLayer() || controllerLayer()")
    public Object logMethodExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();

        // Log method entry with masked parameters
        log.debug("ENTER: {}.{}() with arguments: {}",
                className, methodName, maskSensitiveData(joinPoint.getArgs()));

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        try {
            // Execute the method
            Object result = joinPoint.proceed();
            stopWatch.stop();

            long executionTime = stopWatch.getTotalTimeMillis();

            // Log method exit with execution time
            log.debug("EXIT: {}.{}() executed in {} ms with result: {}",
                    className, methodName, executionTime, maskSensitiveData(result));

            // Log warning for slow methods (performance monitoring)
            if (executionTime > 1000) {
                log.warn("SLOW METHOD: {}.{}() took {} ms",
                        className, methodName, executionTime);
            }

            return result;

        } catch (Exception e) {
            stopWatch.stop();
            log.error("EXCEPTION in {}.{}() after {} ms: {}",
                    className, methodName, stopWatch.getTotalTimeMillis(), e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Masks sensitive data in logs for security
     * Prevents exposure of passwords, tokens, and personal information
     */
    private Object maskSensitiveData(Object data) {
        if (data == null) {
            return null;
        }

        if (data instanceof Object[] array) {
            return Arrays.stream(array)
                    .map(this::maskSingleValue)
                    .toArray();
        }

        return maskSingleValue(data);
    }

    /**
     * Masks individual sensitive values
     */
    private Object maskSingleValue(Object value) {
        if (value == null) {
            return null;
        }

        String stringValue = value.toString();

        // Mask email addresses
        if (stringValue.contains("@")) {
            return maskEmail(stringValue);
        }

        // Mask phone numbers (Uganda format)
        if (stringValue.matches(".*(\\+?256|0)[0-9]{9}.*")) {
            return maskPhoneNumber(stringValue);
        }

        // Mask passwords and tokens
        if (stringValue.toLowerCase().contains("password") ||
                stringValue.toLowerCase().contains("token") ||
                stringValue.toLowerCase().contains("secret")) {
            return "***MASKED***";
        }

        // Return original value if no sensitive data detected
        return value;
    }

    /**
     * Masks email addresses for privacy
     */
    private String maskEmail(String email) {
        if (email == null || email.length() < 6) {
            return "***";
        }

        int atIndex = email.indexOf('@');
        if (atIndex > 3) {
            return email.substring(0, 3) + "***" + email.substring(atIndex);
        }
        return "***" + email.substring(atIndex);
    }

    /**
     * Masks phone numbers for privacy
     */
    private String maskPhoneNumber(String phone) {
        if (phone == null || phone.length() < 6) {
            return "***";
        }

        String cleaned = phone.replaceAll("[^0-9]", "");
        if (cleaned.length() >= 6) {
            return cleaned.substring(0, 3) + "****" + cleaned.substring(cleaned.length() - 3);
        }
        return "***";
    }
}