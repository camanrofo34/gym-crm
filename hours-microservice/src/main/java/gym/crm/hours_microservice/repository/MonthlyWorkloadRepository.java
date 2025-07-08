package gym.crm.hours_microservice.repository;

import gym.crm.hours_microservice.domain.entity.MonthlyWorkload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MonthlyWorkloadRepository extends JpaRepository<MonthlyWorkload,Long> {

    Optional<MonthlyWorkload> findByYearlyWorkloadIdAndTrainignMonth(Long yearlyWorkloadId, String trainignMonth);
}
