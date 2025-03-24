package gym.crm.backend.configuration;

import gym.crm.backend.util.UserUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

@Configuration
@EnableSpringDataWebSupport
public class WebConfig {

    @Bean
    public UserUtil userUtil() {
        return new UserUtil();
    }

}
