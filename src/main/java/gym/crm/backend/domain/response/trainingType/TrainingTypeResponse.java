package gym.crm.backend.domain.response.trainingType;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class TrainingTypeResponse {
    private String trainingTypeName;
    private Long trainingTypeId;
}
