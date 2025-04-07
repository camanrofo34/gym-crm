package gym.crm.backend.domain.response.trainer;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class TraineesTrainerList {
    private String traineeUsername;
    private String traineeFirstName;
    private String traineeLastName;
}
