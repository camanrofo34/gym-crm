package gym.crm.backend.domain.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TrainingCreationRequest {

    @NotBlank(message = "Trainee username cannot be blank")
    private String traineeUsername;

    @NotBlank(message = "Trainer username cannot be blank")
    private String trainerUsername;

    @NotBlank(message = "Training name cannot be blank")
    private String trainingName;

    @NotNull(message = "Training date cannot be blank")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date trainingDate;

    @NotNull(message = "Training duration cannot be blank")
    private Double trainingDuration;
}
