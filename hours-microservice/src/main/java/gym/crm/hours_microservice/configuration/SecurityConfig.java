package gym.crm.hours_microservice.configuration;

import gym.crm.hours_microservice.filter.JwtAuthFilter;
import gym.crm.hours_microservice.filter.TransactionLoggingFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final TransactionLoggingFilter transactionLoggingFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter,
                          TransactionLoggingFilter transactionLoggingFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.transactionLoggingFilter = transactionLoggingFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/**", "/swagger-ui/*",
                                "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/h2-console/**").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(transactionLoggingFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}

