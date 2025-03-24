package gym.crm.backend.repository;

import gym.crm.backend.domain.entities.TrainingType;
import gym.crm.backend.domain.entities.TrainingTypes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

import java.util.Optional;

public interface TrainingTypeRepository extends JpaRepository<TrainingType, Long> {
    Optional<TrainingType> findByTrainingTypeName(TrainingTypes trainingTypeName);

    @NonNull
    Page<TrainingType> findAll (@NonNull Pageable pageable);
}
