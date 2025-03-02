package gym.crm.backend.configuration;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ComponentScan(basePackages = "gym.crm.backend")
@PropertySource("classpath:application.properties")
public class AppConfig {

}
