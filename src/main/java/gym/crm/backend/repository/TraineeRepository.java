package gym.crm.backend.repository;

import gym.crm.backend.domain.Trainee;
import gym.crm.backend.domain.Trainer;
import gym.crm.backend.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TraineeRepository extends JpaRepository<Trainee, Long> {

    Optional<Trainee> findByUserUsername(String username);

//    @Query("update Trainee t set t.user.password = :password where t.user.username = :userUsername")
//    @Modifying
//    void updateUserPasswordByUser_Username(String userUsername, String password);

    @Query("SELECT t FROM Trainer t WHERE t NOT IN " +
            "(SELECT tr FROM Trainer tr JOIN tr.trainees trt WHERE trt.user.username = :traineeUsername)")
    List<Trainer> findTrainersNotInTrainersTraineeListByTraineeUserUsername(String traineeUsername);
}
