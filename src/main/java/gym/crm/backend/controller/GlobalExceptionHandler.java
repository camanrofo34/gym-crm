package gym.crm.backend.controller;

import gym.crm.backend.exception.entityNotFoundException.ProfileNotFoundException;
import gym.crm.backend.exception.entityNotFoundException.TrainingTypeNotFoundException;
import gym.crm.backend.exception.entityNotFoundException.UserNotFoundException;
import gym.crm.backend.exception.runtimeException.PasswordNotCreatedException;
import gym.crm.backend.exception.runtimeException.UsernameNotCreatedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.text.ParseException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PasswordNotCreatedException.class)
    public ResponseEntity<String> handlePasswordNotCreatedException(PasswordNotCreatedException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }

    @ExceptionHandler(ProfileNotFoundException.class)
    public ResponseEntity<String> handleProfileNotFoundException(ProfileNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(TrainingTypeNotFoundException.class)
    public ResponseEntity<String> handleTrainingTypeNotFoundException(TrainingTypeNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(UsernameNotCreatedException.class)
    public ResponseEntity<String> handleUsernameNotCreatedException(UsernameNotCreatedException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<String> handleUserNotFoundException(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(ParseException.class)
    public ResponseEntity<String> handleParseException(ParseException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }
}
