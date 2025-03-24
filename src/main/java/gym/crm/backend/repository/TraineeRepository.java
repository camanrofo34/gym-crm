package gym.crm.backend.repository;

import gym.crm.backend.domain.entities.Trainee;
import gym.crm.backend.domain.entities.Trainer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.Set;

public interface TraineeRepository extends JpaRepository<Trainee, Long> {

    Optional<Trainee> findByUserUsername(String username);

    @Query("SELECT t FROM Trainer t WHERE t NOT IN " +
            "(SELECT tr FROM Trainer tr JOIN tr.trainees trt WHERE trt.user.username = :traineeUsername)")
    Page<Trainer> findTrainersNotInTrainersTraineeListByTraineeUserUsername(String traineeUsername, Pageable pageable);
}
