package gym.crm.backend.configuration;

import gym.crm.backend.util.UserUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
public class AppConfig {

    @Bean
    public UserUtil userUtil() {
        return new UserUtil();
    }

}
