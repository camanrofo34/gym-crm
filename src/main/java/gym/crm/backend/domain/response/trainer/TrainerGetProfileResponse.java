package gym.crm.backend.domain.response.trainer;

import gym.crm.backend.domain.entities.Trainee;
import gym.crm.backend.domain.entities.Trainer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class TrainerGetProfileResponse {
    private String firstName;
    private String lastName;
    private Long specialization;
    private boolean isActive;
    private List<TraineesTrainerList> trainees;

    public TrainerGetProfileResponse(Trainer trainer) {
        this.firstName = trainer.getUser().getFirstName();
        this.lastName = trainer.getUser().getLastName();
        this.specialization = trainer.getSpecialization().getId();
        this.isActive = trainer.getUser().getIsActive();
        this.trainees = new ArrayList<>();
        setTrainees(trainer.getTrainees());
    }

    private void setTrainees(List<Trainee> trainees) {
        trainees.forEach(trainer -> {
            TraineesTrainerList traineesTrainerList = new TraineesTrainerList(
                    trainer.getUser().getUsername(),
                    trainer.getUser().getFirstName(),
                    trainer.getUser().getLastName()
            );
            this.trainees.add(traineesTrainerList);
        });
    }
}
