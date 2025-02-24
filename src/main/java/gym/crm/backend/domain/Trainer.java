package gym.crm.backend.domain;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
public class Trainer extends User{
    private Long trainerId;

    @Setter
    private TrainingType specialization;

    public Trainer(String firstName, String lastName, String specialization) {
        super(firstName, lastName, true);
        this.specialization = new TrainingType(specialization);
    }

    public Trainer(long trainerId, String firstName, String lastName, String specialization){
        super(firstName, lastName, true);
        this.trainerId = trainerId;
        this.specialization = new TrainingType(specialization);
    }
}
