package gym.crm.backend.exception.handler;

import gym.crm.backend.exception.types.notFound.ProfileNotFoundException;
import gym.crm.backend.exception.types.notFound.TrainingTypeNotFoundException;
import gym.crm.backend.exception.types.notFound.UserNotFoundException;
import gym.crm.backend.exception.types.runtime.PasswordNotCreatedException;
import gym.crm.backend.exception.types.runtime.UsernameNotCreatedException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.text.ParseException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(PasswordNotCreatedException.class)
    public ResponseEntity<String> handlePasswordNotCreatedException(PasswordNotCreatedException ex) {
        log.error("Transaction ID: {} - Password not created", MDC.get("transactionId"), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }

    @ExceptionHandler(ProfileNotFoundException.class)
    public ResponseEntity<String> handleProfileNotFoundException(ProfileNotFoundException ex) {
        log.error("Transaction ID: {} - Profile not found", MDC.get("transactionId"), ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(TrainingTypeNotFoundException.class)
    public ResponseEntity<String> handleTrainingTypeNotFoundException(TrainingTypeNotFoundException ex) {
        log.error("Transaction ID: {} - Training type not found", MDC.get("transactionId"), ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(UsernameNotCreatedException.class)
    public ResponseEntity<String> handleUsernameNotCreatedException(UsernameNotCreatedException ex) {
        log.error("Transaction ID: {} - Username not created", MDC.get("transactionId"), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<String> handleUserNotFoundException(UserNotFoundException ex) {
        log.error("Transaction ID: {} - User not found", MDC.get("transactionId"), ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(ParseException.class)
    public ResponseEntity<String> handleParseException(ParseException ex) {
        log.error("Transaction ID: {} - Parse exception", MDC.get("transactionId"), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception ex) {
        log.error("Transaction ID: {} - Internal server error", MDC.get("transactionId"), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }
}
