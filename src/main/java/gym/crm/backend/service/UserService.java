package gym.crm.backend.service;

import gym.crm.backend.domain.entities.Trainer;
import gym.crm.backend.domain.entities.User;
import gym.crm.backend.domain.request.LoginRequest;
import gym.crm.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean login(LoginRequest loginRequest) {
        logger.info("Transaction Id: {}. Login attempt for user: {}", MDC.get("transactionId"),loginRequest.getUsername());
        Optional<User> user = userRepository.findByUsername(loginRequest.getUsername());
        return user.map(value -> value.getPassword().equals(loginRequest.getPassword())).orElse(false);
    }

    public boolean changePassword(LoginRequest loginRequest, String newPassword) {
        try {
            logger.info("Transaction Id: {}. Changing password for user: {}", MDC.get("transactionId"),loginRequest.getUsername());
            Optional<User> user = userRepository.findByUsername(loginRequest.getUsername());
            user.ifPresent(value -> {
                value.setPassword(newPassword);
                userRepository.save(value);
            });
            return true;
        }catch (Exception e){
            logger.error("Transaction Id: {}. Error during password change for user: {}", MDC.get("transactionId"),loginRequest.getUsername(), e);
            return false;
        }
    }

    public void activateDeactivateUser(String username, boolean isActive) {
        Optional<User> user = userRepository.findByUsername(username);
        logger.info("Transaction ID: {}. Toggling trainer status", MDC.get("transactionId"));
        user.ifPresent(t -> {
            t.setIsActive(!isActive);
            userRepository.save(t);
        });
    }
}
