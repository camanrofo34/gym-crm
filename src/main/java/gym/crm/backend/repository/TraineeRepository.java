package gym.crm.backend.repository;

import gym.crm.backend.domain.entities.Trainee;
import gym.crm.backend.domain.entities.Trainer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TraineeRepository extends JpaRepository<Trainee, Long> {

    Optional<Trainee> findByUserUsername(String username);

    @Query("SELECT t FROM Trainer t WHERE t NOT IN " +
            "(SELECT tr FROM Trainer tr JOIN tr.trainees trt WHERE trt.user.username = :traineeUsername)")
    List<Trainer> findTrainersNotInTrainersTraineeListByTraineeUserUsername(String traineeUsername);
}
