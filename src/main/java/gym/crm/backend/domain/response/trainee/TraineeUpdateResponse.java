package gym.crm.backend.domain.response.trainee;

import gym.crm.backend.domain.entities.Trainee;
import gym.crm.backend.domain.entities.Trainer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@AllArgsConstructor
@Getter
@Setter
public class TraineeUpdateResponse {
    private String username;
    private String firstName;
    private String lastName;
    private Date dateOfBirth;
    private String address;
    private boolean isActive;
    private Set<TrainersTraineeResponse> trainers;

    public TraineeUpdateResponse(Trainee trainee) {
        this.username = trainee.getUser().getUsername();
        this.firstName = trainee.getUser().getFirstName();
        this.lastName = trainee.getUser().getLastName();
        this.dateOfBirth = trainee.getDateOfBirth();
        this.address = trainee.getAddress();
        this.isActive = trainee.getUser().getIsActive();
        this.trainers = new HashSet<>();
        setTrainers(trainee.getTrainers());
    }

    public void setTrainers(Set<Trainer> trainers){
        trainers.forEach(trainer -> {
            TrainersTraineeResponse trainersTraineeResponse = new TrainersTraineeResponse(
                    trainer.getUser().getUsername(),
                    trainer.getUser().getFirstName(),
                    trainer.getUser().getLastName(),
                    trainer.getSpecialization().getId()
            );
            this.trainers.add(trainersTraineeResponse);
        });
    }
}
