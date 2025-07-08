package gym.crm.hours_microservice.repository;

import gym.crm.hours_microservice.domain.entity.TrainerWorkloadSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrainerWorkloadSummaryRepository extends JpaRepository<TrainerWorkloadSummary,String> {
}
