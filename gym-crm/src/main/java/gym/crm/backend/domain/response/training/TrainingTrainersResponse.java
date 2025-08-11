package gym.crm.backend.domain.response.training;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@AllArgsConstructor
@Getter
@NoArgsConstructor
@Setter
public class TrainingTrainersResponse {
    private String TrainingName;
    private Date TrainingDate;
    private String TrainingType;
    private Double TrainingDuration;
    private String TraineeName;
}
