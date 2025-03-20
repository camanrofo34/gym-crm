package gym.crm.backend.domain.response.trainee;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TrainersTraineeResponse {
    private String trainerUsername;
    private String trainerFirstName;
    private String trainerLastName;
    private Long trainerSpecialization;
}