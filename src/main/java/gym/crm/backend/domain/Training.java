package gym.crm.backend.domain;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
public class Training {
    private long trainingId;
    private long traineeId;
    private long trainerId;

    @Setter
    private String trainingName;

    @Setter
    private TrainingType trainingType;

    @Setter
    private String trainingDate;

    @Setter
    private double trainingDuration;
}
