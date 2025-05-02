package gym.crm.backend.service;

import gym.crm.backend.domain.entities.User;
import gym.crm.backend.domain.request.LoginRequest;
import gym.crm.backend.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class UserService{

    private final UserRepository userRepository;
    private final UserDetailsServiceImpl userDetailsService;
    private static final int MAX_ATTEMPTS = 3;
    private static final long BLOCK_TIME_MS = TimeUnit.MINUTES.toMillis(5);

    private final ConcurrentHashMap<String, Integer> attempts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> blockTimestamps = new ConcurrentHashMap<>();

    @Autowired
    public UserService(UserRepository userRepository, UserDetailsServiceImpl userDetailsService) {
        this.userDetailsService = userDetailsService;
        this.userRepository = userRepository;
    }

    public boolean login(LoginRequest loginRequest) {
        String transactionId = MDC.get("transactionId");
        log.info("Transaction Id: {}. Login attempt for user: {}", transactionId, loginRequest.getUsername());

        if (isBlocked(loginRequest.getUsername())) {
            log.error("Transaction Id: {}. User {} is blocked", transactionId, loginRequest.getUsername());
            throw new RuntimeException("User is blocked");
        }

        try {
            UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getUsername());
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authToken);

            loginSucceeded(loginRequest.getUsername());
            log.info("Transaction Id: {}. User {} logged in successfully", transactionId, loginRequest.getUsername());
            return true;

        } catch (BadCredentialsException e) {
            loginFailed(loginRequest.getUsername());
            log.error("Transaction Id: {}. Invalid credentials for user: {}", transactionId, loginRequest.getUsername());
            return false;
        } catch (AuthenticationException e) {
            loginFailed(loginRequest.getUsername());
            log.error("Transaction Id: {}. Authentication failed for user: {}. Reason: {}", transactionId, loginRequest.getUsername(), e.getMessage());
            return false;
        }
    }


    public void changePassword(LoginRequest loginRequest, String newPassword) {
        String transactionId = MDC.get("transactionId");
        log.info("Transaction Id: {}. Changing password for user: {}", transactionId, loginRequest.getUsername());

        User user = userRepository.findByUsername(loginRequest.getUsername()).orElseThrow(() ->
        {
            log.error("Transaction Id: {}. User not found: {}", transactionId, loginRequest.getUsername());
            return new RuntimeException("User with username: " + loginRequest.getUsername() + " not found");
        });
        if (!user.getPassword().equals(loginRequest.getPassword())) {
            log.error("Transaction Id: {}. Password mismatch for user: {}", transactionId, loginRequest.getUsername());
            throw new RuntimeException("Password mismatch");
        }
        user.setPassword(newPassword);
        userRepository.save(user);
    }

    public void activateDeactivateUser(String username, boolean isActive) {
        String transactionId = MDC.get("transactionId");
        log.info("Transaction ID: {}. Toggling trainer status", transactionId);

        User user = userRepository.findByUsername(username).orElseThrow(() ->
        {
            log.error("Transaction Id: {}. User not found: {}", transactionId, username);
            return new RuntimeException("User with username: " + username + " not found");
        });

        user.setIsActive(!isActive);
        userRepository.save(user);
    }

    private void loginFailed(String username) {
        int currentAttempts = attempts.getOrDefault(username, 0);
        attempts.put(username, currentAttempts + 1);
        if (currentAttempts + 1 >= MAX_ATTEMPTS) {
            blockTimestamps.put(username, System.currentTimeMillis());
        }
    }

    private void loginSucceeded(String username) {
        attempts.remove(username);
        blockTimestamps.remove(username);
    }

    private boolean isBlocked(String username) {
        Long blockedSince = blockTimestamps.get(username);
        if (blockedSince == null) {
            return false;
        }
        if ((System.currentTimeMillis() - blockedSince) >= BLOCK_TIME_MS) {
            loginSucceeded(username);
            return false;
        }
        return true;
    }
}
