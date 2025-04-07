package gym.crm.backend.repository;

import gym.crm.backend.domain.entities.Training;
import gym.crm.backend.domain.entities.TrainingType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;

public interface TrainingRepository extends JpaRepository<Training, Long> {

    @Query("SELECT t FROM Training t WHERE t.trainee.user.username = :username " +
            "AND (:fromDate IS NULL OR t.trainingDate >= :fromDate) " +
            "AND (:toDate IS NULL OR t.trainingDate <= :toDate) " +
            "AND (:trainerName IS NULL OR t.trainer.user.firstName LIKE %:trainerName%) " +
            "AND (:trainingType IS NULL OR t.trainingType.trainingTypeName = :trainingType)")
    Page<Training> findTraineeTrainings(String username, Date fromDate, Date toDate, String trainerName, TrainingType trainingType, Pageable pageable);

    @Query("SELECT t FROM Training t WHERE t.trainer.user.username = :username " +
            "AND (:fromDate IS NULL OR t.trainingDate >= :fromDate) " +
            "AND (:toDate IS NULL OR t.trainingDate <= :toDate) " +
            "AND (:traineeName IS NULL OR t.trainee.user.firstName LIKE %:traineeName%)")
    Page<Training> findTrainerTrainings(String username, Date fromDate, Date toDate, String traineeName, Pageable pageable);
}
