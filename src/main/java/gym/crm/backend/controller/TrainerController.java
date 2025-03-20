package gym.crm.backend.controller;

import gym.crm.backend.domain.request.TrainerCreationRequest;
import gym.crm.backend.domain.request.TrainerUpdateRequest;
import gym.crm.backend.domain.response.training.TrainingTrainersResponse;
import gym.crm.backend.service.TrainerService;
import gym.crm.backend.service.TrainingService;
import gym.crm.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/trainer")
@Tag( name = "Trainer", description = "Operations related to trainer management")
public class TrainerController {

    private final TrainerService trainerService;
    private final TrainingService trainingService;
    private final UserService userService;
    private final Logger logger = LoggerFactory.getLogger(TrainerController.class);

    @Autowired
    public TrainerController(TrainerService trainerService, TrainingService trainingService, UserService userService) {
        this.trainerService = trainerService;
        this.trainingService = trainingService;
        this.userService = userService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new trainer", description = "Creates a new trainer in the system.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trainer registered successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> registerTrainer(@RequestBody @Valid TrainerCreationRequest trainerRequest) {

        String transactionId = UUID.randomUUID().toString();

        MDC.put("transactionId", transactionId);

        logger.info("Transaction ID: {} - Registering trainer", transactionId);
        try {
            return trainerService.createTrainer(trainerRequest)
                    .map(userCreationResponse -> {
                        logger.info("Transaction ID: {} - Trainer registered successfully: {}", transactionId, userCreationResponse.getUsername());
                        return ResponseEntity.ok(userCreationResponse);
                    })
                    .orElseThrow(() -> new IllegalStateException("Could not create trainer"));
        } catch (Exception e) {
            logger.error("Transaction ID: {} - Error: {}", transactionId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not create trainer due to an internal error");
        } finally {
            MDC.clear();
        }
    }

    @GetMapping("profile/{username}")
    @Operation(summary = "Get trainer profile", description = "Fetches the profile of a trainer by username.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trainer profile fetched successfully"),
            @ApiResponse(responseCode = "404", description = "Trainer not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getTrainer(@PathVariable String username) {

        String transactionId = UUID.randomUUID().toString();

        MDC.put("transactionId", transactionId);

        logger.info("Transaction ID: {} - Fetching trainer profile for: {}", transactionId, username);
        try {
            return trainerService.getTrainerByUsername(username)
                    .map(ResponseEntity::ok)
                    .orElseThrow(() -> new EntityNotFoundException("Trainer not found: " + username));
        } catch (EntityNotFoundException e) {
            logger.error("Transaction ID: {} - Trainer not found: {}", transactionId, username);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Trainer not found: " + username);
        } catch (Exception e) {
            logger.error("Transaction ID: {} - Error: {}", transactionId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not fetch trainer profile due to an internal error");
        } finally {
            MDC.clear();
        }
    }

    @PutMapping("profile/{username}")
    @Operation(summary = "Update trainer profile", description = "Updates the profile of a trainer by username.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trainer profile updated successfully"),
            @ApiResponse(responseCode = "404", description = "Trainer not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> updateTrainer(@PathVariable String username, @Valid @RequestBody TrainerUpdateRequest trainerRequest) {
        String transactionId = UUID.randomUUID().toString();

        MDC.put("transactionId", transactionId);

        logger.info("Transaction ID: {} - Updating trainer: {}", transactionId, username);
        try {
            return trainerService.updateTrainerProfile(username, trainerRequest)
                    .map(ResponseEntity::ok)
                    .orElseThrow(() -> new EntityNotFoundException("Trainer not found: " + username));
        } catch (EntityNotFoundException e) {
            logger.error("Transaction ID: {} - Trainer not found: {}", transactionId, username);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Trainer not found: " + username);
        } catch (Exception e) {
            logger.error("Transaction ID: {} - Error: {}", transactionId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not update trainer profile due to an internal error");
        } finally {
            MDC.clear();
        }
    }

    @GetMapping("profile/{username}/trainings")
    @Operation(summary = "Get trainer trainings", description = "Fetches the trainings of a trainer by username.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trainer trainings fetched successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getTrainerTrainings(@PathVariable String username,
                                                 @RequestParam(required = false) String fromDate,
                                                 @RequestParam(required = false) String toDate,
                                                 @RequestParam(required = false) String traineeName) {
        String transactionId = UUID.randomUUID().toString();

        MDC.put("transactionId", transactionId);

        logger.info("Transaction ID: {} - Fetching trainings for trainer: {}", transactionId, username);
        try {
            List<TrainingTrainersResponse> trainings = trainingService.getTrainerTrainings(username, fromDate, toDate, traineeName);
            return ResponseEntity.ok(trainings);
        } catch (Exception e) {
            logger.error("Transaction ID: {} - Error: {}", transactionId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not fetch trainings due to an internal error");
        } finally {
            MDC.clear();
        }
    }

    @PatchMapping("profile/{username}/activate-deactivate")
    @Operation(summary = "Activate/Deactivate trainer", description = "Toggles the status of a trainer by username.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trainer status toggled successfully"),
            @ApiResponse(responseCode = "404", description = "Trainer not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> activateDeactivateTrainer(@PathVariable String username, @RequestParam boolean isActive) {
        String transactionId = UUID.randomUUID().toString();

        MDC.put("transactionId", transactionId);

        logger.info("Transaction ID: {} - Toggling trainer status for: {}", transactionId, username);
        try {
            userService.activateDeactivateUser(username, isActive);
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e) {
            logger.error("Transaction ID: {} - Trainer not found: {}", transactionId, username);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Trainer not found: " + username);
        } catch (Exception e) {
            logger.error("Transaction ID: {} - Error: {}", transactionId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not toggle trainer status due to an internal error");
        } finally {
            MDC.clear();
        }
    }
}
