package gym.crm.backend.repository;

import gym.crm.backend.domain.Training;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;

public interface TrainingRepository extends JpaRepository<Training, Long> {

    @Query("SELECT t FROM Training t WHERE t.trainee.user.username = :username " +
            "AND (:fromDate IS NULL OR t.trainingDate >= :fromDate) " +
            "AND (:toDate IS NULL OR t.trainingDate <= :toDate) " +
            "AND (:trainerName IS NULL OR t.trainer.user.firstName LIKE %:trainerName%) " +
            "AND (:trainingType IS NULL OR t.trainingType.trainingTypeName = :trainingType)")
    List<Training> findTraineeTrainings(String username, Date fromDate, Date toDate, String trainerName, String trainingType);

    @Query("SELECT t FROM Training t WHERE t.trainer.user.username = :username " +
            "AND (:fromDate IS NULL OR t.trainingDate >= :fromDate) " +
            "AND (:toDate IS NULL OR t.trainingDate <= :toDate) " +
            "AND (:traineeName IS NULL OR t.trainee.user.firstName LIKE %:traineeName%)")
    List<Training> findTrainerTrainings(String username, Date fromDate, Date toDate, String traineeName);
}
