package gym.crm.hours_microservice.domain.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import gym.crm.hours_microservice.domain.entity.ActionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Schema(description = "Request object for updating trainer workload")
public class TrainerWorkloadRequest {
    @NotBlank(message = "Trainer username cannot be blank")
    @Schema(description = "Username of the trainer", example = "john_doe")
    private String trainerUsername;

    @NotBlank(message = "Trainer first name cannot be blank")
    @Schema(description = "First name of the trainer", example = "John")
    private String trainerFirstName;

    @NotBlank(message = "Trainer last name cannot be blank")
    @Schema(description = "Last name of the trainer", example = "Doe")
    private String trainerLastName;

    @NotNull(message = "isActive cannot be null")
    @Schema(description = "Indicates if the trainer is currently active", example = "true")
    private Boolean isActive;

    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Schema(description = "Date of the training session", example = "2023-10-01")
    private Date trainingDate;

    @NotNull(message = "Training duration cannot be blank")
    @Schema(description = "Duration of the training session in hours", example = "1.5")
    private Double trainingDuration;

    @NotNull(message = "Action type cannot be blank")
    @Schema(description = "Action type for the workload update", example = "ADD or DELETE")
    private ActionType actionType;

}
