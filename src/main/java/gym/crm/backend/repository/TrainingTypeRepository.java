package gym.crm.backend.repository;

import gym.crm.backend.domain.entities.TrainingType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainingTypeRepository extends JpaRepository<TrainingType, Long> {
}
