package gym.crm.backend.controller;

import gym.crm.backend.domain.request.TrainingCreationRequest;
import gym.crm.backend.domain.response.trainingType.TrainingTypeResponse;
import gym.crm.backend.service.TrainingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/training")
@Tag(name = "Training", description = "Operations related to training management")
public class TrainingController {

    private final TrainingService trainingService;
    private final Logger logger = LoggerFactory.getLogger(TrainingController.class);

    @Autowired
    public TrainingController(TrainingService trainingService) {
        this.trainingService = trainingService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new training", description = "Creates a new training in the system.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Training registered successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> registerTraining(@RequestBody @Valid TrainingCreationRequest trainingCreationRequest) {
        String transactionId = UUID.randomUUID().toString();
        MDC.put("transactionId", transactionId);
        logger.info("Transaction ID: {} - Registering training", transactionId);
        try {
            trainingService.createTraining(trainingCreationRequest);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Transaction ID: {} - Error: {}", transactionId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not create training due to an internal error");
        }finally {
            MDC.clear();
        }
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
        logger.info("Transaction ID: {} - Fetching training types", transactionId);
        try {
            List<TrainingTypeResponse> trainingTypes = trainingService.getTrainingTypes();
            return ResponseEntity.ok(trainingTypes);
        } catch (Exception e) {
            logger.error("Transaction ID: {} - Error: {}", transactionId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not fetch training types due to an internal error");
        }finally {
            MDC.clear();
        }
    }
}
