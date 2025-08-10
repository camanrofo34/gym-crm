package gym.crm.backend.domain.response.trainee;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@AllArgsConstructor
@Setter
@NoArgsConstructor
public class TrainersTraineeResponse {
    private String trainerUsername;
    private String trainerFirstName;
    private String trainerLastName;
    private Long trainerSpecialization;
}