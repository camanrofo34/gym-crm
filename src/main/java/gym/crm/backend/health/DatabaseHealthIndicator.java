package gym.crm.backend.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Component
public class DatabaseHealthIndicator implements HealthIndicator {

    private final DataSource dataSource;

    public DatabaseHealthIndicator(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Health health() {
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(1)) {
                return Health.up()
                        .withDetail("database", "Available")
                        .withDetail("system", "Running")
                        .build();
            }
        } catch (SQLException e) {
            return Health.down()
                    .withDetail("database", "Not Available")
                    .withDetail("error", e.getMessage())
                    .build();
        }
        return Health.unknown().build();
    }

}
