package gym.crm.backend.domain.response.trainer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class TraineesTrainerList {
    private String traineeUsername;
    private String traineeFirstName;
    private String traineeLastName;
}
