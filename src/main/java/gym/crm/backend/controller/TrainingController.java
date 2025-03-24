package gym.crm.backend.controller;

import gym.crm.backend.domain.request.TrainingCreationRequest;
import gym.crm.backend.domain.response.trainingType.TrainingTypeResponse;
import gym.crm.backend.service.TrainingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/training")
@Slf4j
@Tag(name = "Training", description = "Operations related to training management")
public class TrainingController {

    private final TrainingService trainingService;

    @Autowired
    public TrainingController(TrainingService trainingService) {
        this.trainingService = trainingService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new training", description = "Creates a new training in the system.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Training registered successfully"),
            @ApiResponse(responseCode = "404", description = "Trainee/Trainer not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> registerTraining(@RequestBody @Valid TrainingCreationRequest trainingCreationRequest) {
        String transactionId = UUID.randomUUID().toString();
        MDC.put("transactionId", transactionId);
        log.info("Transaction ID: {} - Registering training", transactionId);

        trainingService.createTraining(trainingCreationRequest);

        log.info("Transaction ID: {} - Training registered successfully", transactionId);
        MDC.clear();
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/trainingTypes")
    @Operation(summary = "Get training types", description = "Fetches all available training types.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Training types fetched successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getTrainingTypes() {
        String transactionId = UUID.randomUUID().toString();
        MDC.put("transactionId", transactionId);

        log.info("Transaction ID: {} - Fetching training types", transactionId);

        Set<TrainingTypeResponse> trainingTypes = trainingService.getTrainingTypes();

        CollectionModel<TrainingTypeResponse> responses = CollectionModel.of(
                trainingTypes,
                linkTo(methodOn(TrainingController.class).getTrainingTypes()).withSelfRel()
        );

        log.info("Transaction ID: {} - Training types fetched successfully", transactionId);
        MDC.clear();

        return ResponseEntity.status(HttpStatus.OK).body(responses);
    }
}
