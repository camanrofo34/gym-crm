package gym.crm.backend.repository;

import gym.crm.backend.domain.entities.Trainer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TrainerRepository extends JpaRepository<Trainer, Long> {

    Optional<Trainer> findByUserUsername(String username);

}
