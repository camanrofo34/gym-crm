package gym.crm.backend.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class MemoryHealthIndicator implements HealthIndicator {

    private static final long THRESHOLD = 50 * 1024 * 1024;

    @Override
    public Health health() {
        long freeMemory = Runtime.getRuntime().freeMemory();
        long totalMemory = Runtime.getRuntime().totalMemory();
        long usedMemory = totalMemory - freeMemory;

        if (freeMemory >= THRESHOLD) {
            return Health.up()
                    .withDetail("Used Memory", usedMemory / (1024 * 1024) + " MB")
                    .withDetail("Free Memory", freeMemory / (1024 * 1024) + " MB")
                    .build();
        } else {
            return Health.down()
                    .withDetail("Used Memory", usedMemory / (1024 * 1024) + " MB")
                    .withDetail("Free Memory", freeMemory / (1024 * 1024) + " MB (Low)")
                    .build();
        }
    }
}
