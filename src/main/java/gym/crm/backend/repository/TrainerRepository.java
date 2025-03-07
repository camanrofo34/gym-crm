package gym.crm.backend.repository;

import gym.crm.backend.domain.Trainer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TrainerRepository extends JpaRepository<Trainer, Long> {

    Optional<Trainer> findByUserUsername(String username);

//    @Query("update Trainer t set t.user.password = :password where t.user.username = :userUsername")
//    @Modifying
//    void updateUserPasswordByUser_Username(String userUsername, String password);

}
