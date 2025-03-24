package gym.crm.backend.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import java.io.File;

@Component
public class DiskSpaceHealthIndicator implements HealthIndicator {

    private static final long THRESHOLD = 500 * 1024 * 1024;

    @Override
    public Health health() {
        File disk = new File("/");
        long freeSpace = disk.getFreeSpace();

        if (freeSpace >= THRESHOLD) {
            return Health.up().withDetail("freeSpace", freeSpace / (1024 * 1024) + " MB").build();
        } else {
            return Health.down().withDetail("freeSpace", freeSpace / (1024 * 1024) + " MB").build();
        }
    }
}
