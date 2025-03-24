package gym.crm.backend.service;

import gym.crm.backend.domain.entities.User;
import gym.crm.backend.domain.request.LoginRequest;
import gym.crm.backend.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean login(LoginRequest loginRequest) {
        String transactionId = MDC.get("transactionId");
        log.info("Transaction Id: {}. Login attempt for user: {}", transactionId,loginRequest.getUsername());

        User user = userRepository.findByUsername(loginRequest.getUsername()).orElseThrow(() ->
        {
            log.error("Transaction Id: {}. User not found: {}", transactionId, loginRequest.getUsername());
            return new RuntimeException("User with username: " + loginRequest.getUsername() + " not found");
        });

        return user.getPassword().equals(loginRequest.getPassword());
    }

    public void changePassword(LoginRequest loginRequest, String newPassword) {
        String transactionId = MDC.get("transactionId");
        log.info("Transaction Id: {}. Changing password for user: {}", transactionId, loginRequest.getUsername());
        User user = userRepository.findByUsername(loginRequest.getUsername()).orElseThrow(() ->
        {
            log.error("Transaction Id: {}. User not found: {}", transactionId, loginRequest.getUsername());
            return new RuntimeException("User with username: " + loginRequest.getUsername() + " not found");
        });
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
}
