package gym.crm.backend.controller;

import gym.crm.backend.domain.request.LoginRequest;
import gym.crm.backend.service.JwtService;
import gym.crm.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/user")
@Slf4j
@Tag(name = "User", description = "Operations related to user management")
public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Autowired
    public UserController(UserService userService,
                          PasswordEncoder passwordEncoder,
                          JwtService jwtService) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
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
            String token = jwtService.generateToken(loginRequest.getUsername());
            MDC.clear();
            return ResponseEntity.status(HttpStatus.OK).body(token);
        } else {
            log.error("Transaction ID: {} - User authentication failed", transactionId);
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
    @PreAuthorize("hasRole('ROLE_TRAINER') or hasRole('ROLE_TRAINEE')")
    public ResponseEntity<Void> changePassword(@RequestBody @Valid LoginRequest loginRequest, @RequestParam String newPassword) {
        String transactionId = UUID.randomUUID().toString();
        MDC.put("transactionId", transactionId);

        log.info("Transaction ID: {} - Change password request: {}", transactionId, loginRequest.getUsername());

        loginRequest.setPassword(passwordEncoder.encode(loginRequest.getPassword()));
        newPassword = passwordEncoder.encode(newPassword);

        userService.changePassword(loginRequest, newPassword);

        log.info("Transaction ID: {} - Password changed successfully", transactionId);
        MDC.clear();
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout user", description = "Logs out the user and invalidates the session.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User logged out successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('ROLE_TRAINER') or hasRole('ROLE_TRAINEE')")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        if (authentication != null) {
            new SecurityContextLogoutHandler().logout(request, response, authentication);
        }
        return ResponseEntity.ok("Logout successful");
    }
}
