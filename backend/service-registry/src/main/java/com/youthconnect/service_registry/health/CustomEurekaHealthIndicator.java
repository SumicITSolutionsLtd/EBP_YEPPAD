package com.youthconnect.service_registry.health;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Custom Eureka Health Indicator for Service Registry
 * Renamed to avoid conflict with Spring Cloud's auto-configured bean
 */
@Slf4j
@Component("customEurekaHealthIndicator")  // ← RENAMED HERE
@RequiredArgsConstructor
public class CustomEurekaHealthIndicator implements HealthIndicator {

    private final EurekaClient eurekaClient;

    @Override
    public Health health() {
        try {
            InstanceInfo instanceInfo = eurekaClient.getApplicationInfoManager()
                    .getInfo();

            if (instanceInfo == null) {
                log.warn("Eureka instance info is null");
                return Health.down()
                        .withDetail("error", "Instance info not available")
                        .build();
            }

            InstanceInfo.InstanceStatus status = instanceInfo.getStatus();

            if (status == InstanceInfo.InstanceStatus.UP) {
                return Health.up()
                        .withDetail("status", status.name())
                        .withDetail("instanceId", instanceInfo.getInstanceId())
                        .withDetail("ipAddr", instanceInfo.getIPAddr())
                        .withDetail("port", instanceInfo.getPort())
                        .withDetail("registeredServices",
                                eurekaClient.getApplications().size())
                        .build();
            } else {
                return Health.down()
                        .withDetail("status", status.name())
                        .withDetail("instanceId", instanceInfo.getInstanceId())
                        .build();
            }

        } catch (Exception e) {
            log.error("Error checking Eureka health", e);
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}