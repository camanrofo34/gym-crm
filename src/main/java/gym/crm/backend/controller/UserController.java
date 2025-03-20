package gym.crm.backend.controller;

import gym.crm.backend.domain.request.LoginRequest;
import gym.crm.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/user")
@Tag(name = "User", description =    "Operations related to user management")
public class UserController {

    private final UserService userService;
    private final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user", description = "Validates user credentials and returns authentication result.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User authenticated successfully"),
            @ApiResponse(responseCode = "401", description = "User authentication failed"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequest loginRequest) {
        String transactionId = UUID.randomUUID().toString();
        MDC.put("transactionId", transactionId);
        logger.info("Transaction ID: {} .Login attempt for user: {}", transactionId, loginRequest.getUsername());
        try {
            boolean isAuthenticated = userService.login(loginRequest);
            logger.info("Transaction ID: {} .Login attempt for user: {} - Success: {}", transactionId ,loginRequest.getUsername(), isAuthenticated);
            return isAuthenticated ? ResponseEntity.ok().build() : ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            logger.error("Transaction ID: {} .Error during login process for user: {}", transactionId,loginRequest.getUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Login process failed.");
        } finally {
            MDC.clear();
        }
    }

    @PutMapping("/change-password")
    @Operation(summary = "Change user password", description = "Allows users to change their password providing the correct credentials.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password changed successfully"),
            @ApiResponse(responseCode = "400", description = "Password change failed"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> changePassword(@RequestBody @Valid LoginRequest loginRequest, @RequestParam String newPassword) {
        String transactionId = UUID.randomUUID().toString();
        MDC.put("transactionId", transactionId);
        logger.info("Transaction ID: {}, Password change attempt for user: {}", transactionId, loginRequest.getUsername());
        try {
            boolean isChanged = userService.changePassword(loginRequest, newPassword);
            logger.info("Transaction ID: {}, Password change attempt for user: {} - Success: {}", transactionId, loginRequest.getUsername(), isChanged);
            return isChanged ? ResponseEntity.ok().build() : ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Password change failed.");
        } catch (Exception e) {
            logger.error("Error during password change for user: {}", loginRequest.getUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Password change process failed.");
        } finally {
            MDC.clear();
        }
    }
}
