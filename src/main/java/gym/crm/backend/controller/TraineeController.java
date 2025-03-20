package gym.crm.backend.controller;

import gym.crm.backend.domain.request.TraineeCreationRequest;
import gym.crm.backend.domain.request.TraineeUpdateRequest;
import gym.crm.backend.domain.response.trainee.TrainersTraineeResponse;
import gym.crm.backend.domain.response.training.TrainingTraineesResponse;
import gym.crm.backend.service.TraineeService;
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

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/trainee")
@Tag(name = "Trainee", description = "Operations related to trainee management")
public class TraineeController {

    private final TraineeService traineeService;
    private final TrainingService trainingService;
    private final UserService userService;
    private final Logger logger = LoggerFactory.getLogger(TraineeController.class.getName());

    @Autowired
    public TraineeController(TraineeService traineeService, TrainingService trainingService, UserService userService) {
        this.traineeService = traineeService;
        this.trainingService = trainingService;
        this.userService = userService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register trainee", description = "Creates a new trainee user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trainee created successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> registerTrainee(@Valid @RequestBody TraineeCreationRequest traineeCreationRequest) {
        String transactionId = UUID.randomUUID().toString();

        MDC.put("transactionId", transactionId);

        logger.info("Transaction ID: {} - Received request to register trainee: {}", transactionId, traineeCreationRequest);

        try {
            return traineeService.createTrainee(traineeCreationRequest)
                    .map(trainee -> {
                        logger.info("Transaction ID: {} - Trainee registered successfully: {}", transactionId, trainee.getUsername());
                        return ResponseEntity.ok(trainee);
                    })
                    .orElseThrow(() -> new IllegalStateException("Could not create trainee"));
        } catch (Exception e) {
            logger.error("Transaction ID: {} - Error registering trainee: {}", transactionId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not create trainee due to an internal error");
        } finally {
            MDC.clear();
        }
    }


    @GetMapping("/profile/{username}")
    @Operation(summary = "Fetch trainee profile", description = "Returns the profile of a trainee user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trainee profile fetched successfully"),
            @ApiResponse(responseCode = "404", description = "Trainee not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getTrainee(@PathVariable String username) {

        String transactionId = UUID.randomUUID().toString();

        MDC.put("transactionId", transactionId);

        logger.info("Transaction ID: {} - Fetching trainee profile for: {}", transactionId, username);

        try{
            return traineeService.getTraineeByUsername(username)
                    .map(traineeGetProfileResponse -> {
                        logger.info("Transaction ID: {} - Trainee profile fetched successfully: {}", transactionId, traineeGetProfileResponse);
                        return ResponseEntity.ok(traineeGetProfileResponse);
                    })
                    .orElseThrow(() -> new EntityNotFoundException("Trainee not found: " + username));
        } catch (Exception e) {
            logger.error("Transaction ID: {} - Error: {}", transactionId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not fetch trainee profile due an internal error");
        }finally {
            MDC.clear();
        }
    }

    @PutMapping("/profile/{username}")
    @Operation(summary = "Update trainee profile", description = "Updates the profile of a trainee user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trainee profile updated successfully"),
            @ApiResponse(responseCode = "404", description = "Trainee not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> updateTrainee(@PathVariable String username, @Valid @RequestBody TraineeUpdateRequest traineeUpdateRequest) {

        String transactionId = UUID.randomUUID().toString();

        MDC.put("transactionId", transactionId);

        logger.info("Transaction ID: {} - Updating trainee: {}", transactionId, username);

        try{
            return traineeService.updateTrainee(username, traineeUpdateRequest)
                    .map(traineeUpdateResponse -> {
                        logger.info("Transaction ID: {} - Trainee updated successfully: {}", transactionId, traineeUpdateResponse);
                        return ResponseEntity.ok(traineeUpdateResponse);
                    })
                    .orElseThrow(() -> new EntityNotFoundException("Trainee not found: " + username));

        } catch (Exception e) {
            logger.error("Transaction ID: {} - Error: {}", transactionId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not update trainee profile due an internal error");
        } finally {
            MDC.clear();
        }
    }

    @DeleteMapping("/profile/{username}")
    @Operation(summary = "Delete trainee", description = "Deletes a trainee user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Trainee deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Trainee not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> deleteTrainee(@PathVariable String username) {

        String transactionId = UUID.randomUUID().toString();

        MDC.put("transactionId", transactionId);

        logger.info("Transaction ID: {} - Deleting trainee: {}", transactionId, username);
        try {
            traineeService.deleteTrainee(username);
            logger.info("Transaction ID: {} - Trainee deleted successfully: {}", transactionId, username);
            return ResponseEntity.noContent().build();
        }catch (EntityNotFoundException e) {
            logger.error("Transaction ID: {} - Trainee not found: {}", transactionId, username);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Trainee not found: " + username);
        } catch (Exception e) {
            logger.error("Transaction ID: {} - Error: {}", transactionId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not delete trainee due an internal error");
        }finally {
            MDC.clear();
        }
    }

    @GetMapping("/profile/{username}/not-assigned-trainers")
    @Operation(summary = "Fetch trainers not assigned to trainee", description = "Returns a list of trainers not assigned to a trainee user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trainers fetched successfully"),
            @ApiResponse(responseCode = "404", description = "Trainee not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getTrainersNotAssignedToTrainee(@PathVariable String username) {
        String transactionId = UUID.randomUUID().toString();

        MDC.put("transactionId", transactionId);

        logger.info("Transaction ID: {} - Searching Trainers Not Assigned to: {}", transactionId, username);
        try {
            List<TrainersTraineeResponse> trainers = traineeService.getTrainersNotInTrainersTraineeListByTraineeUserUsername(username);
            logger.info("Transaction ID: {} - Trainers fetched successfully: {}", transactionId, trainers);
            if (trainers.isEmpty()) {
                return ResponseEntity.ok(Collections.emptyList());
            }
            return ResponseEntity.ok(trainers);
        } catch (EntityNotFoundException e) {
            logger.error("Transaction ID: {} - Trainee not found: {}", transactionId, username);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Trainee not found: " + username);
        } catch (Exception e) {
            logger.error("Transaction ID: {} - Error: {}", transactionId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not fetch trainers due to an internal error");
        } finally {
            MDC.clear();
        }
    }


    @PutMapping("/profile/{username}/assign-trainers")
    @Operation(summary = "Assign trainers to trainee", description = "Assigns trainers to a trainee user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trainers assigned successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> assignTrainersToTrainee(@PathVariable String username, @Valid @RequestBody List<String> trainerUsernames) {
        String transactionId = UUID.randomUUID().toString();

        MDC.put("transactionId", transactionId);

        logger.info("Transaction ID: {} - Assigning trainers to: {}", transactionId, username);
        try {
            return ResponseEntity.ok(traineeService.updateTrainersTraineeList(username, trainerUsernames));
        }catch (EntityNotFoundException e) {
            logger.error("Transaction ID: {} - Trainee not found: {}", transactionId, username);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Trainee not found: " + username);
        }catch (Exception e){
            logger.error("Transaction ID: {} - Error: {}", transactionId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not assign trainers due an internal error");
        }finally {
            MDC.clear();
        }
    }

    @GetMapping("/profile/{username}/trainings")
    @Operation(summary = "Fetch trainings for trainee", description = "Returns a list of trainings for a trainee user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trainings fetched successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<TrainingTraineesResponse>> getTrainingsForTrainee(
            @PathVariable String username,
            @RequestParam(required = false) String periodFrom,
            @RequestParam(required = false) String periodTo,
            @RequestParam(required = false) String trainerName,
            @RequestParam(required = false) String trainingType) {
        String transactionId = UUID.randomUUID().toString();

        MDC.put("transactionId", transactionId);

        logger.info("Transaction ID: {} - Fetching trainings for trainee: {}", transactionId, username);
        try {
            return ResponseEntity.ok(trainingService.getTraineeTrainings(username, periodFrom, periodTo, trainerName, trainingType));
        }catch (EntityNotFoundException e) {
            logger.error("Transaction ID: {} - Trainee not found: {}", transactionId, username);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyList());
        }catch (Exception e){
            logger.error("Transaction ID: {} - Error: {}", transactionId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }finally {
            MDC.clear();
        }

    }

    @PatchMapping("/profile/{username}/activate-deactivate")
    @Operation(summary = "Activate or deactivate trainee", description = "Activates or deactivates a trainee user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trainee status toggled successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> activateDeactivateTrainee(@PathVariable String username, @RequestParam boolean isActive) {
        String transactionId = UUID.randomUUID().toString();

        MDC.put("transactionId", transactionId);

        logger.info("Transaction ID: {} - Toggling trainee status for: {}", transactionId, username);
        try {
            userService.activateDeactivateUser(username, isActive);
            return ResponseEntity.ok().build();
        }catch (EntityNotFoundException e) {
            logger.error("Transaction ID: {} - Trainee not found: {}", transactionId, username);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Trainee not found: " + username);
        }catch (Exception e){
            logger.error("Transaction ID: {} - Error: {}", transactionId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not toggle trainee status due an internal error");
        } finally {
            MDC.clear();
        }
    }
}

