package gym.crm.backend.controller;

import gym.crm.backend.domain.request.LoginRequest;
import gym.crm.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/user")
@Slf4j
@Tag(name = "User", description = "Operations related to user management")
public class UserController {

    private final UserService userService;

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

        log.info("Transaction ID: {} - Login request: {}", transactionId, loginRequest.getUsername());

        boolean isAuthenticated = userService.login(loginRequest);

        if (isAuthenticated) {
            log.info("Transaction ID: {} - User authenticated successfully", transactionId);
            MDC.clear();
            return ResponseEntity.status(HttpStatus.OK).build();
        } else {
            log.info("Transaction ID: {} - User authentication failed", transactionId);
            MDC.clear();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
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

        log.info("Transaction ID: {} - Change password request: {}", transactionId, loginRequest.getUsername());

        userService.changePassword(loginRequest, newPassword);

        log.info("Transaction ID: {} - Password changed successfully", transactionId);
        MDC.clear();
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
