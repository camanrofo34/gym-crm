package gym.crm.backend.configuration;

import gym.crm.backend.dao.InMemoryStorage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ComponentScan(basePackages = "gym.crm.backend")
@PropertySource("classpath:application.properties")
public class AppConfig {

    @Bean
    public InMemoryStorage inMemoryStorage() {
        return new InMemoryStorage();
    }
}
