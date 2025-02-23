package gym.crm.backend.domain;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Trainer extends User{
    private Long trainerId;
    private TrainingType specialization;

    public Trainer(String firstName, String lastName, String specialization) {
        super(firstName, lastName, true);
        TrainingType trainingType = new TrainingType();
        trainingType.setTrainingTypeName(specialization);
        this.specialization = trainingType;
    }
}
