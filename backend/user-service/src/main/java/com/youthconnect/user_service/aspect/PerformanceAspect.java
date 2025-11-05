package com.youthconnect.user_service.aspect;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Performance Monitoring Aspect for Youth Connect Uganda User Service
 *
 * Features:
 * - Method execution time tracking with Micrometer metrics
 * - Performance threshold monitoring and alerts
 * - Database query performance monitoring
 * - Cache performance metrics
 * - Service-level performance indicators
 *
 * @author Douglas Kings Kato
 * @version 1.0.0
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class PerformanceAspect {

    private final MeterRegistry meterRegistry;

    // Thread-safe storage for timers
    private final ConcurrentMap<String, Timer> timerCache = new ConcurrentHashMap<>();

    /**
     * Pointcut for critical business methods that need performance monitoring
     */
    @Pointcut("execution(* com.youthconnect.user_service.service.UserService.*(..))")
    public void userServiceMethods() {}

    /**
     * Pointcut for database repository methods
     */
    @Pointcut("execution(* com.youthconnect.user_service.repository..*(..))")
    public void repositoryMethods() {}

    /**
     * Pointcut for external service calls
     */
    @Pointcut("execution(* com.youthconnect.user_service.client..*(..))")
    public void externalServiceCalls() {}

    /**
     * Performance monitoring for critical business methods
     */
    @Around("userServiceMethods() || repositoryMethods() || externalServiceCalls()")
    public Object monitorPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String metricName = "com.youthconnect.method.execution";

        // Get or create timer for this method
        Timer timer = timerCache.computeIfAbsent(className + "." + methodName,
                key -> Timer.builder(metricName)
                        .tag("class", className)
                        .tag("method", methodName)
                        .tag("service", "user-service")
                        .register(meterRegistry));

        // Execute method with timing
        return timer.record(() -> {
            try {
                long startTime = System.currentTimeMillis();
                Object result = joinPoint.proceed();
                long executionTime = System.currentTimeMillis() - startTime;

                // Log performance metrics for slow operations
                logPerformanceMetrics(className, methodName, executionTime, joinPoint.getArgs());

                return result;

            } catch (Throwable e) {
                // Record failure in metrics
                meterRegistry.counter("youthconnect.method.errors",
                        "class", className,
                        "method", methodName,
                        "exception", e.getClass().getSimpleName()
                ).increment();

                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Logs performance metrics and triggers alerts for slow operations
     */
    private void logPerformanceMetrics(String className, String methodName,
                                       long executionTime, Object[] args) {

        // Categorize performance
        PerformanceCategory category = categorizePerformance(executionTime, methodName);

        switch (category) {
            case EXCELLENT:
                log.debug("PERFORMANCE: {}.{}() - {} ms [EXCELLENT]",
                        className, methodName, executionTime);
                break;

            case GOOD:
                log.debug("PERFORMANCE: {}.{}() - {} ms [GOOD]",
                        className, methodName, executionTime);
                break;

            case ACCEPTABLE:
                log.info("PERFORMANCE: {}.{}() - {} ms [ACCEPTABLE]",
                        className, methodName, executionTime);
                break;

            case SLOW:
                log.warn("PERFORMANCE: {}.{}() - {} ms [SLOW - Needs optimization]",
                        className, methodName, executionTime);
                break;

            case CRITICAL:
                log.error("PERFORMANCE: {}.{}() - {} ms [CRITICAL - Immediate attention required]",
                        className, methodName, executionTime);
                break;
        }

        // Record metrics based on category
        meterRegistry.counter("youthconnect.performance.category",
                "class", className,
                "method", methodName,
                "category", category.name()
        ).increment();
    }

    /**
     * Categorizes performance based on execution time and method type
     */
    private PerformanceCategory categorizePerformance(long executionTime, String methodName) {
        // Different thresholds for different types of methods
        long excellentThreshold = getExcellentThreshold(methodName);
        long goodThreshold = getGoodThreshold(methodName);
        long acceptableThreshold = getAcceptableThreshold(methodName);
        long criticalThreshold = getCriticalThreshold(methodName);

        if (executionTime <= excellentThreshold) {
            return PerformanceCategory.EXCELLENT;
        } else if (executionTime <= goodThreshold) {
            return PerformanceCategory.GOOD;
        } else if (executionTime <= acceptableThreshold) {
            return PerformanceCategory.ACCEPTABLE;
        } else if (executionTime <= criticalThreshold) {
            return PerformanceCategory.SLOW;
        } else {
            return PerformanceCategory.CRITICAL;
        }
    }

    /**
     * Performance thresholds based on method type
     */
    private long getExcellentThreshold(String methodName) {
        if (methodName.startsWith("get") || methodName.startsWith("find")) {
            return 50; // 50ms for read operations
        } else if (methodName.startsWith("create") || methodName.startsWith("save")) {
            return 100; // 100ms for write operations
        } else {
            return 80; // 80ms for other operations
        }
    }

    private long getGoodThreshold(String methodName) {
        if (methodName.startsWith("get") || methodName.startsWith("find")) {
            return 100; // 100ms for read operations
        } else if (methodName.startsWith("create") || methodName.startsWith("save")) {
            return 200; // 200ms for write operations
        } else {
            return 150; // 150ms for other operations
        }
    }

    private long getAcceptableThreshold(String methodName) {
        if (methodName.startsWith("get") || methodName.startsWith("find")) {
            return 500; // 500ms for read operations
        } else if (methodName.startsWith("create") || methodName.startsWith("save")) {
            return 1000; // 1000ms for write operations
        } else {
            return 800; // 800ms for other operations
        }
    }

    private long getCriticalThreshold(String methodName) {
        if (methodName.startsWith("get") || methodName.startsWith("find")) {
            return 2000; // 2 seconds for read operations
        } else if (methodName.startsWith("create") || methodName.startsWith("save")) {
            return 5000; // 5 seconds for write operations
        } else {
            return 3000; // 3 seconds for other operations
        }
    }

    /**
     * Performance categories for method execution times
     */
    private enum PerformanceCategory {
        EXCELLENT, GOOD, ACCEPTABLE, SLOW, CRITICAL
    }
}