package gym.crm.hours_microservice.controller;

import gym.crm.hours_microservice.domain.request.TrainerWorkloadRequest;
import gym.crm.hours_microservice.service.TrainerWorkloadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/working-hours")
@Slf4j

@Tag(name = "Working Hours", description = "Operations related to working hours of the trainers from the gym")
public class WorkingHoursController {

    private final TrainerWorkloadService trainerWorkloadService;

    @Autowired
    private WorkingHoursController(TrainerWorkloadService trainerWorkloadService) {
        this.trainerWorkloadService = trainerWorkloadService;
    }

    @PostMapping
    @Operation(summary = "Update trainer workload", description = "Adds or removes training hours for a trainer, based on action type (ADD or DELETE)")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Successfully saved the working hours"),
                    @ApiResponse(responseCode = "400", description = "Bad request due to invalid input data"),
                    @ApiResponse(responseCode = "500", description = "Internal server error due to unexpected issues")
            }
    )
    public ResponseEntity<String> updateTrainerWorkload(@RequestBody @Valid TrainerWorkloadRequest trainerWorkloadRequest) {
        String transactionId = MDC.get("transactionId");
        log.info("Transaction ID: {} - Updating trainer workload for request: {}", transactionId, trainerWorkloadRequest.getTrainerUsername());
        trainerWorkloadService.updateTrainerWorkload(trainerWorkloadRequest);
        log.info("Transaction ID: {} - Successfully updated trainer workload", transactionId);
        return ResponseEntity.status(200).build();
    }
}
