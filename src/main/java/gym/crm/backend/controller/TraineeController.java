package gym.crm.backend.controller;

import gym.crm.backend.domain.request.TraineeCreationRequest;
import gym.crm.backend.domain.request.TraineeUpdateRequest;
import gym.crm.backend.domain.response.UserCreationResponse;
import gym.crm.backend.domain.response.trainee.TraineeGetProfileResponse;
import gym.crm.backend.domain.response.trainee.TraineeUpdateResponse;
import gym.crm.backend.domain.response.trainee.TrainersTraineeResponse;
import gym.crm.backend.domain.response.training.TrainingTraineesResponse;
import gym.crm.backend.service.TraineeService;
import gym.crm.backend.service.TrainingService;
import gym.crm.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/trainee")
@Slf4j
@Validated

@Tag(name = "Trainee", description = "Operations related to trainee management")
public class TraineeController {

    private final TraineeService traineeService;
    private final TrainingService trainingService;
    private final UserService userService;

    @Autowired
    public TraineeController(TraineeService traineeService, TrainingService trainingService, UserService userService) {
        this.traineeService = traineeService;
        this.trainingService = trainingService;
        this.userService = userService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register trainee", description = "Creates a new trainee user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Trainee created successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> registerTrainee(@Valid @RequestBody TraineeCreationRequest traineeCreationRequest) {
        String transactionId = UUID.randomUUID().toString();

        MDC.put("transactionId", transactionId);

        log.info("Transaction ID: {} - Received request to register trainee: {}", transactionId, traineeCreationRequest);

        UserCreationResponse userCreationResponse = traineeService.createTrainee(traineeCreationRequest);
        EntityModel<UserCreationResponse> response = EntityModel.of(
                userCreationResponse,
                linkTo(methodOn(TraineeController.class).registerTrainee(traineeCreationRequest)).withSelfRel(),
                linkTo(methodOn(TraineeController.class).getTrainee(userCreationResponse.getUsername())).withRel("trainee-profile"),
                linkTo(methodOn(TraineeController.class).deleteTrainee(userCreationResponse.getUsername())).withRel("delete-profile"),
                linkTo(methodOn(TraineeController.class).getTrainingsForTrainee(userCreationResponse.getUsername(), null, null, null, null)).withRel("trainings"),
                linkTo(methodOn(TraineeController.class).getTrainersNotAssignedToTrainee(userCreationResponse.getUsername())).withRel("trainers-not-assigned")
        );

        log.info("Transaction ID: {} - Trainee created successfully: {}", transactionId, userCreationResponse);
        MDC.clear();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
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

        log.info("Transaction ID: {} - Fetching trainee: {}", transactionId, username);

        TraineeGetProfileResponse traineeGetProfileResponse = traineeService.getTraineeByUsername(username);

        EntityModel<TraineeGetProfileResponse> response = EntityModel.of(
                traineeGetProfileResponse,
                linkTo(methodOn(TraineeController.class).getTrainee(username)).withSelfRel(),
                linkTo(methodOn(TraineeController.class).deleteTrainee(username)).withRel("delete-profile"),
                linkTo(methodOn(TraineeController.class).getTrainingsForTrainee(username, null, null, null, null)).withRel("trainings"),
                linkTo(methodOn(TraineeController.class).getTrainersNotAssignedToTrainee(username)).withRel("trainers-not-assigned")
        );

        log.info("Transaction ID: {} - Trainee fetched successfully: {}", transactionId, traineeGetProfileResponse);
        MDC.clear();
        return ResponseEntity.status(HttpStatus.OK).body(response);
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

        log.info("Transaction ID: {} - Updating trainee: {}", transactionId, traineeUpdateRequest);

        TraineeUpdateResponse traineeUpdateResponse = traineeService.updateTrainee(username, traineeUpdateRequest);

        EntityModel<TraineeUpdateResponse> response = EntityModel.of(
                traineeUpdateResponse,
                linkTo(methodOn(TraineeController.class).updateTrainee(username, traineeUpdateRequest)).withSelfRel(),
                linkTo(methodOn(TraineeController.class).getTrainee(username)).withRel("trainee-profile"),
                linkTo(methodOn(TraineeController.class).deleteTrainee(username)).withRel("delete-profile"),
                linkTo(methodOn(TraineeController.class).getTrainingsForTrainee(username, null, null, null, null)).withRel("trainings"),
                linkTo(methodOn(TraineeController.class).getTrainersNotAssignedToTrainee(username)).withRel("trainers-not-assigned")
        );

        log.info("Transaction ID: {} - Trainee updated successfully: {}", transactionId, traineeUpdateResponse);
        MDC.clear();
        return ResponseEntity.status(HttpStatus.OK).body(response);
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

        log.info("Transaction ID: {} - Deleting trainee: {}", transactionId, username);

        traineeService.deleteTrainee(username);

        log.info("Transaction ID: {} - Trainee deleted successfully: {}", transactionId, username);
        MDC.clear();
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
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

        log.info("Transaction ID: {} - Fetching trainers not assigned to trainee: {}", transactionId, username);

        Set<TrainersTraineeResponse> trainers = traineeService.getTrainersNotInTrainersTraineeListByTraineeUserUsername(username);
        CollectionModel<TrainersTraineeResponse> responses = CollectionModel.of(
                trainers,
                linkTo(methodOn(TraineeController.class).getTrainersNotAssignedToTrainee(username)).withSelfRel(),
                linkTo(methodOn(TraineeController.class).getTrainee(username)).withRel("trainee-profile")
        );

        log.info("Transaction ID: {} - Trainers fetched successfully: {}", transactionId, trainers);
        MDC.clear();
        return ResponseEntity.status(HttpStatus.OK).body(responses);
    }


    @PutMapping("/profile/{username}/assign-trainers")
    @Operation(summary = "Assign trainers to trainee", description = "Assigns trainers to a trainee user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trainers assigned successfully"),
            @ApiResponse(responseCode = "404", description = "Trainee/Trainer not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> assignTrainersToTrainee(@PathVariable String username, @Valid @RequestBody List<String> trainerUsernames) {
        String transactionId = UUID.randomUUID().toString();

        MDC.put("transactionId", transactionId);

        log.info("Transaction ID: {} - Assigning trainers to trainee: {}", transactionId, username);

        Set<TrainersTraineeResponse> trainers = traineeService.updateTrainersTraineeList(username, trainerUsernames);
        CollectionModel<TrainersTraineeResponse> responses = CollectionModel.of(
                trainers,
                linkTo(methodOn(TraineeController.class).assignTrainersToTrainee(username, trainerUsernames)).withSelfRel(),
                linkTo(methodOn(TraineeController.class).getTrainee(username)).withRel("trainee-profile")
        );

        log.info("Transaction ID: {} - Trainers assigned successfully: {}", transactionId, trainers);
        MDC.clear();
        return ResponseEntity.status(HttpStatus.OK).body(responses);
    }

    @GetMapping("/profile/{username}/trainings")
    @Operation(summary = "Fetch trainings for trainee", description = "Returns a list of trainings for a trainee user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trainings fetched successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request for the Date written"),
            @ApiResponse(responseCode = "404", description = "Trainee not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getTrainingsForTrainee(
            @PathVariable String username,
            @RequestParam(required = false) String periodFrom,
            @RequestParam(required = false) String periodTo,
            @RequestParam(required = false) String trainerName,
            @RequestParam(required = false) String trainingType) {
        String transactionId = UUID.randomUUID().toString();

        MDC.put("transactionId", transactionId);

        log.info("Transaction ID: {} - Fetching trainings for trainee: {}", transactionId, username);

        Set<TrainingTraineesResponse> trainings = trainingService.getTraineeTrainings(username, periodFrom, periodTo, trainerName, trainingType);
        CollectionModel<TrainingTraineesResponse> responses = CollectionModel.of(
                trainings,
                linkTo(methodOn(TraineeController.class).getTrainingsForTrainee(username, periodFrom, periodTo, trainerName, trainingType)).withSelfRel(),
                linkTo(methodOn(TraineeController.class).getTrainee(username)).withRel("trainee-profile")
        );

        log.info("Transaction ID: {} - Trainings fetched successfully: {}", transactionId, trainings);
        MDC.clear();
        return ResponseEntity.status(HttpStatus.OK).body(responses);
    }

    @PatchMapping("/profile/{username}/activate-deactivate")
    @Operation(summary = "Activate or deactivate trainee", description = "Activates or deactivates a trainee user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trainee status toggled successfully"),
            @ApiResponse(responseCode = "404", description = "Trainee not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> activateDeactivateTrainee(@PathVariable String username, @RequestParam boolean isActive) {
        String transactionId = UUID.randomUUID().toString();

        MDC.put("transactionId", transactionId);

        log.info("Transaction ID: {} - Toggling trainer status: {}", transactionId, username);

        userService.activateDeactivateUser(username, isActive);

        log.info("Transaction ID: {} - Trainee toggled status successfully: {}", transactionId, username);
        MDC.clear();
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}

