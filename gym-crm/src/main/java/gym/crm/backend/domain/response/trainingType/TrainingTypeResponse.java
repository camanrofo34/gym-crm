package gym.crm.backend.domain.response.trainingType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class TrainingTypeResponse {
    private String trainingTypeName;
    private Long trainingTypeId;
}
