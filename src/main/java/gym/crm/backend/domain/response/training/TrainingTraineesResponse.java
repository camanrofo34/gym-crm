package gym.crm.backend.domain.response.training;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@AllArgsConstructor
@Getter
@Setter
public class TrainingTraineesResponse {
    private String TrainingName;
    private Date TrainingDate;
    private String TrainingType;
    private Double TrainingDuration;
    private String TrainerName;
}
