package gym.crm.backend.controller;

import gym.crm.backend.domain.request.TrainerCreationRequest;
import gym.crm.backend.domain.request.TrainerUpdateRequest;
import gym.crm.backend.domain.response.UserCreationResponse;
import gym.crm.backend.domain.response.trainer.TrainerGetProfileResponse;
import gym.crm.backend.domain.response.trainer.TrainerUpdateResponse;
import gym.crm.backend.domain.response.training.TrainingTrainersResponse;
import gym.crm.backend.service.TrainerService;
import gym.crm.backend.service.TrainingService;
import gym.crm.backend.service.UserService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/trainer")
@Slf4j
@Validated
@Tag( name = "Trainer", description = "Operations related to trainer management")
public class TrainerController {

    private final TrainerService trainerService;
    private final TrainingService trainingService;
    private final UserService userService;

    private final Counter trainerRegistrationCounter;
    private final Timer trainerRegistrationTimer;

    private PagedResourcesAssembler<TrainingTrainersResponse> pagedResourcesAssemblerTraining;

    @Autowired
    public TrainerController(TrainerService trainerService,
                             TrainingService trainingService,
                             UserService userService,
                             MeterRegistry meterRegistry) {
        this.trainerService = trainerService;
        this.trainingService = trainingService;
        this.userService = userService;

        this.trainerRegistrationCounter = meterRegistry.counter("trainer.registration.counter");
        this.trainerRegistrationTimer = meterRegistry.timer("trainer.registration.timer");
    }

    @Autowired
    public void setPagedResourcesAssemblerTraining(PagedResourcesAssembler<TrainingTrainersResponse> pagedResourcesAssemblerTraining) {
        this.pagedResourcesAssemblerTraining = pagedResourcesAssemblerTraining;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new trainer", description = "Creates a new trainer in the system.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Trainer registered successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<EntityModel<UserCreationResponse>> registerTrainer(@RequestBody @Valid TrainerCreationRequest trainerRequest) {
        String transactionId = UUID.randomUUID().toString();

        long startTime = System.nanoTime();
        MDC.put("transactionId", transactionId);

        log.info("Transaction ID: {} - Registering trainer: {}", transactionId, trainerRequest.getFirstName());

        UserCreationResponse userCreationResponse = trainerService.createTrainer(trainerRequest);
        EntityModel<UserCreationResponse> response = EntityModel.of(
                userCreationResponse,
                linkTo(methodOn(TrainerController.class).registerTrainer(trainerRequest)).withSelfRel(),
                linkTo(methodOn(TrainerController.class).getTrainer(userCreationResponse.getUsername())).withRel("trainer-profile"),
                linkTo(methodOn(TrainerController.class).getTrainerTrainings(userCreationResponse.getUsername(), null, null, null, Pageable.unpaged())).withRel("trainer-trainings")
        );

        log.info("Transaction ID: {} - Trainer registered successfully: {}", transactionId, userCreationResponse.getUsername());
        trainerRegistrationCounter.increment();
        MDC.clear();
        trainerRegistrationTimer.record(System.nanoTime() - startTime, TimeUnit.NANOSECONDS);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("profile/{username}")
    @Operation(summary = "Get trainer profile", description = "Fetches the profile of a trainer by username.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trainer profile fetched successfully"),
            @ApiResponse(responseCode = "404", description = "Trainer not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('ROLE_TRAINER')")
    public ResponseEntity<EntityModel<TrainerGetProfileResponse>> getTrainer(@PathVariable String username) {

        String transactionId = UUID.randomUUID().toString();

        MDC.put("transactionId", transactionId);

        log.info("Transaction ID: {} - Fetching trainer profile: {}", transactionId, username);

        TrainerGetProfileResponse trainerGetProfileResponse = trainerService.getTrainerByUsername(username);
        EntityModel<TrainerGetProfileResponse> response = EntityModel.of(
                trainerGetProfileResponse,
                linkTo(methodOn(TrainerController.class).getTrainer(username)).withSelfRel(),
                linkTo(methodOn(TrainerController.class).getTrainerTrainings(username, null, null, null, Pageable.unpaged())).withRel("trainer-trainings")
        );

        log.info("Transaction ID: {} - Trainer profile fetched successfully: {}", transactionId, username);
        MDC.clear();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("profile/{username}")
    @Operation(summary = "Update trainer profile", description = "Updates the profile of a trainer by username.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trainer profile updated successfully"),
            @ApiResponse(responseCode = "404", description = "Trainer not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('ROLE_TRAINER')")
    public ResponseEntity<EntityModel<TrainerUpdateResponse>> updateTrainer(@PathVariable String username, @Valid @RequestBody TrainerUpdateRequest trainerRequest) {
        String transactionId = UUID.randomUUID().toString();

        MDC.put("transactionId", transactionId);

        log.info("Transaction ID: {} - Updating trainer profile: {}", transactionId, username);
        TrainerUpdateResponse trainerUpdateResponse = trainerService.updateTrainerProfile(username, trainerRequest);
        EntityModel<TrainerUpdateResponse> response = EntityModel.of(
                trainerUpdateResponse,
                linkTo(methodOn(TrainerController.class).updateTrainer(username, trainerRequest)).withSelfRel(),
                linkTo(methodOn(TrainerController.class).getTrainer(username)).withRel("trainer-profile"),
                linkTo(methodOn(TrainerController.class).getTrainerTrainings(username, null, null, null, Pageable.unpaged())).withRel("trainer-trainings")
        );

        log.info("Transaction ID: {} - Trainer profile updated successfully: {}", transactionId, username);
        MDC.clear();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("profile/{username}/trainings")
    @Operation(summary = "Get trainer trainings", description = "Fetches the trainings of a trainer by username.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trainer trainings fetched successfully"),
            @ApiResponse(responseCode = "404", description = "Trainer not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('ROLE_TRAINER')")
    public ResponseEntity<PagedModel<EntityModel<TrainingTrainersResponse>>> getTrainerTrainings(@PathVariable String username,
                                                          @RequestParam(required = false) String fromDate,
                                                          @RequestParam(required = false) String toDate,
                                                          @RequestParam(required = false) String traineeName,
                                                          @PageableDefault(sort = "trainingDate", direction = Sort.Direction.DESC) Pageable pageable) {
        String transactionId = UUID.randomUUID().toString();

        MDC.put("transactionId", transactionId);

        log.info("Transaction ID: {} - Fetching trainer trainings: {}", transactionId, username);

        Page<TrainingTrainersResponse> trainingTrainersResponses = trainingService.getTrainerTrainings(username, fromDate, toDate, traineeName, pageable);

        PagedModel<EntityModel<TrainingTrainersResponse>> response = pagedResourcesAssemblerTraining.toModel(trainingTrainersResponses);

        log.info("Transaction ID: {} - Trainer trainings fetched successfully: {}", transactionId, username);
        MDC.clear();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PatchMapping("profile/{username}/activate-deactivate")
    @Operation(summary = "Activate/Deactivate trainer", description = "Toggles the status of a trainer by username.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trainer status toggled successfully"),
            @ApiResponse(responseCode = "404", description = "Trainer not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('ROLE_TRAINER')")
    public ResponseEntity<Void> activateDeactivateTrainer(@PathVariable String username, @RequestParam boolean isActive) {
        String transactionId = UUID.randomUUID().toString();

        MDC.put("transactionId", transactionId);

        log.info("Transaction ID: {} - Toggling trainer status: {}", transactionId, username);

        userService.activateDeactivateUser(username, isActive);

        log.info("Transaction ID: {} - Trainer toggled status successfully: {}", transactionId, username);
        MDC.clear();
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
