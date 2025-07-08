package gym.crm.hours_microservice.repository;

import gym.crm.hours_microservice.domain.entity.YearlyWorkload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface YearlyWorkloadRepository extends JpaRepository<YearlyWorkload,Long> {

    Optional<YearlyWorkload> findByTrainerTrainerUsernameAndTrainingYear(String trainerTrainerUsername, String trainingYear);
}
