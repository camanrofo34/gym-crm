package gym.crm.hours_microservice.repository;

import gym.crm.hours_microservice.domain.entity.TrainerWorkloadSummary;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TrainerWorkloadSummaryRepository extends MongoRepository<TrainerWorkloadSummary,String> {

    Optional<TrainerWorkloadSummary> findByTrainerUsername(String trainerUsername);
}
