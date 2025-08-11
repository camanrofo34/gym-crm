package gym.crm.backend.exception.handler;

import gym.crm.backend.exception.types.forbidden.ForbidenException;
import gym.crm.backend.exception.types.notFound.ProfileNotFoundException;
import gym.crm.backend.exception.types.notFound.TrainingTypeNotFoundException;
import gym.crm.backend.exception.types.notFound.UserNotFoundException;
import gym.crm.backend.exception.types.runtime.PasswordNotCreatedException;
import gym.crm.backend.exception.types.runtime.UsernameNotCreatedException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.SocketTimeoutException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
@Tag(name = "Global Exception Handler", description = "Handles exceptions globally for the application")
public class GlobalExceptionHandler {

    @ExceptionHandler(PasswordNotCreatedException.class)
    @Operation(summary = "Handle Password Not Created Exception",
            description = "Handles exceptions when a password cannot be created.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "500", description = "Internal server error due to password creation failure")
    })
    public ResponseEntity<String> handlePasswordNotCreatedException(PasswordNotCreatedException ex) {
        log.error("Transaction ID: {} - Password not created", MDC.get("transactionId"), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }

    @ExceptionHandler(ProfileNotFoundException.class)
    @Operation(summary = "Handle Profile Not Found Exception",
            description = "Handles exceptions when a user profile is not found.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "Profile not found")
    })
    public ResponseEntity<String> handleProfileNotFoundException(ProfileNotFoundException ex) {
        log.error("Transaction ID: {} - Profile not found", MDC.get("transactionId"), ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @Operation(summary = "Handle Training Type Not Found Exception",
            description = "Handles exceptions when a training type is not found.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "Training type not found")
    })
    @ExceptionHandler(TrainingTypeNotFoundException.class)
    public ResponseEntity<String> handleTrainingTypeNotFoundException(TrainingTypeNotFoundException ex) {
        log.error("Transaction ID: {} - Training type not found", MDC.get("transactionId"), ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(UsernameNotCreatedException.class)
    @Operation(summary = "Handle Username Not Created Exception",
            description = "Handles exceptions when a username cannot be created.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "500", description = "Internal server error due to username creation failure")
    })
    public ResponseEntity<String> handleUsernameNotCreatedException(UsernameNotCreatedException ex) {
        log.error("Transaction ID: {} - Username not created", MDC.get("transactionId"), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
    @Operation(summary = "Handle User Not Found Exception",
            description = "Handles exceptions when a user is not found.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<String> handleUserNotFoundException(UserNotFoundException ex) {
        log.error("Transaction ID: {} - User not found", MDC.get("transactionId"), ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(ParseException.class)
    @Operation(summary = "Handle Parse Exception",
            description = "Handles exceptions related to parsing errors.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Bad request due to parsing error")
    })
    public ResponseEntity<String> handleParseException(ParseException ex) {
        log.error("Transaction ID: {} - Parse exception", MDC.get("transactionId"), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(ForbidenException.class)
    @Operation(summary = "Handle Forbidden Exception",
            description = "Handles exceptions when access is forbidden.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "403", description = "Forbidden access")
    })
    public ResponseEntity<String> handleForbiddenException(ForbidenException ex) {
        log.error("Transaction ID: {} - Forbidden access", MDC.get("transactionId"), ex);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @Operation(summary = "Handle General Exception",
            description = "Handles all other exceptions that are not specifically handled.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<String> handleException(Exception ex) {
        log.error("Transaction ID: {} - Internal server error", MDC.get("transactionId"), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    @Operation(summary = "Handle Runtime Exception",
            description = "Handles runtime exceptions that occur during the application execution.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "500", description = "Internal server error due to runtime exception")
    })
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        log.error("Transaction ID: {} - Runtime exception occurred", MDC.get("transactionId"), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @Operation(summary = "Handle Method Argument Not Valid Exception",
            description = "Handles exceptions when method arguments are not valid.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Bad request due to invalid method arguments")
    })
    public ResponseEntity<Object> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(DataAccessResourceFailureException.class)
    @Operation(summary = "Handle Data Access Resource Failure Exception",
            description = "Handles exceptions related to data access resource failures.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "500", description = "Internal server error due to data access resource failure")
    })
    public ResponseEntity<String> handleDataAccessResourceFailureException(DataAccessResourceFailureException ex) {
        log.error("Transaction ID: {} - Data access resource failure", MDC.get("transactionId"), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Database connection error: " + ex.getMessage());
    }

    @ExceptionHandler(SocketTimeoutException.class)
    @Operation(summary = "Handle Socket Timeout Exception",
            description = "Handles exceptions when a socket timeout occurs.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "504", description = "Gateway timeout due to socket timeout")
    })
    public ResponseEntity<String> handleSocketTimeoutException(SocketTimeoutException ex) {
        log.error("Transaction ID: {} - Socket timeout", MDC.get("transactionId"), ex);
        return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).body("Timeout error: " + ex.getMessage());
    }
}
