package gym.crm.backend.controller;

import gym.crm.backend.domain.request.TrainingCreationRequest;
import gym.crm.backend.domain.response.trainingType.TrainingTypeResponse;
import gym.crm.backend.service.TrainingService;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;
import java.util.concurrent.TimeUnit;


@RestController
@RequestMapping("/training")
@Slf4j
@Tag(name = "Training", description = "Operations related to training management")
public class TrainingController {

    private final TrainingService trainingService;

    private final Counter trainingRegistrationCounter;
    private final Timer trainingRegistrationTimer;

    private PagedResourcesAssembler<TrainingTypeResponse> pagedResourcesAssemblerTrainingType;

    @Autowired
    public TrainingController(TrainingService trainingService,
                              MeterRegistry meterRegistry) {
        this.trainingService = trainingService;

        this.trainingRegistrationCounter = meterRegistry.counter("training.registration.counter");
        this.trainingRegistrationTimer = meterRegistry.timer("training.registration.timer");
    }

    @Autowired
    public void setPagedResourcesAssemblerTrainingType(PagedResourcesAssembler<TrainingTypeResponse> pagedResourcesAssemblerTrainingType) {
        this.pagedResourcesAssemblerTrainingType = pagedResourcesAssemblerTrainingType;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new training", description = "Creates a new training in the system.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Training registered successfully"),
            @ApiResponse(responseCode = "404", description = "Trainee/Trainer not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('ROLE_TRAINER') or hasRole('ROLE_TRAINEE')")
    public ResponseEntity<?> registerTraining(@RequestBody @Valid TrainingCreationRequest trainingCreationRequest) {
        String transactionId = UUID.randomUUID().toString();
        long startTime = System.nanoTime();
        MDC.put("transactionId", transactionId);
        log.info("Transaction ID: {} - Registering training", transactionId);

        trainingService.createTraining(trainingCreationRequest);

        log.info("Transaction ID: {} - Training registered successfully", transactionId);
        MDC.clear();
        trainingRegistrationCounter.increment();
        trainingRegistrationTimer.record(System.nanoTime() - startTime, TimeUnit.NANOSECONDS);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/trainingTypes")
    @Operation(summary = "Get training types", description = "Fetches all available training types.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Training types fetched successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('ROLE_TRAINER') or hasRole('ROLE_TRAINEE')")
    public ResponseEntity<PagedModel<EntityModel<TrainingTypeResponse>>> getTrainingTypes(
            @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        String transactionId = UUID.randomUUID().toString();
        MDC.put("transactionId", transactionId);

        log.info("Transaction ID: {} - Fetching training types", transactionId);

        Page<TrainingTypeResponse> trainingTypes = trainingService.getTrainingTypes(pageable);

        PagedModel<EntityModel<TrainingTypeResponse>> responses = pagedResourcesAssemblerTrainingType.toModel(trainingTypes);

        log.info("Transaction ID: {} - Training types fetched successfully", transactionId);
        MDC.clear();

        return ResponseEntity.status(HttpStatus.OK).body(responses);
    }
}
