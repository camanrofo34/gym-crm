package gym.crm.backend.domain;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
public class TrainingType {
    private long trainingTypeId;

    @Setter
    private String TrainingTypeName;

    public TrainingType(String specialization) {
        this.TrainingTypeName = specialization;
    }
}
