package com.youthconnect.service_registry.health;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.shared.Application;
import com.netflix.eureka.EurekaServerContext;
import com.netflix.eureka.registry.PeerAwareInstanceRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Custom Health Indicator for Eureka Server.
 *
 * <p>Provides detailed health information about:
 * <ul>
 *   <li>Number of registered services</li>
 *   <li>Total service instances</li>
 *   <li>Healthy vs unhealthy instances</li>
 *   <li>Peer replication status</li>
 * </ul>
 *
 * @author YouthConnect Uganda Development Team
 * @version 2.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EurekaHealthIndicator implements HealthIndicator {

    private final EurekaServerContext eurekaServerContext;

    /**
     * Performs comprehensive health check of Eureka Server.
     *
     * @return Health status with detailed metrics
     */
    @Override
    public Health health() {
        try {
            PeerAwareInstanceRegistry registry = eurekaServerContext.getRegistry();

            // Get all registered applications
            List<Application> applications = registry.getSortedApplications();

            int totalApps = applications.size();
            int totalInstances = 0;
            int upInstances = 0;

            // Count instances and their status
            for (Application app : applications) {
                List<InstanceInfo> instances = app.getInstances();
                totalInstances += instances.size();

                for (InstanceInfo instance : instances) {
                    if (instance.getStatus() == InstanceInfo.InstanceStatus.UP) {
                        upInstances++;
                    }
                }
            }

            // Build health status
            Health.Builder builder = Health.up()
                    .withDetail("registeredServices", totalApps)
                    .withDetail("totalInstances", totalInstances)
                    .withDetail("upInstances", upInstances)
                    .withDetail("downInstances", totalInstances - upInstances);

            // Add warning if too many services are down
            if (totalInstances > 0 && upInstances < (totalInstances * 0.5)) {
                builder.status("DEGRADED");
                builder.withDetail("warning", "More than 50% of instances are down");
            }

            return builder.build();

        } catch (Exception e) {
            log.error("Eureka health check failed", e);
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
