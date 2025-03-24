package gym.crm.backend.service;


import gym.crm.backend.domain.entities.Trainer;
import gym.crm.backend.domain.entities.TrainingType;
import gym.crm.backend.domain.entities.User;
import gym.crm.backend.domain.request.TrainerCreationRequest;
import gym.crm.backend.domain.request.TrainerUpdateRequest;
import gym.crm.backend.domain.response.trainer.TrainerGetProfileResponse;
import gym.crm.backend.domain.response.UserCreationResponse;
import gym.crm.backend.domain.response.trainer.TrainerUpdateResponse;
import gym.crm.backend.exception.runtimeException.PasswordNotCreatedException;
import gym.crm.backend.exception.entityNotFoundException.ProfileNotFoundException;
import gym.crm.backend.exception.entityNotFoundException.TrainingTypeNotFoundException;
import gym.crm.backend.exception.runtimeException.UsernameNotCreatedException;
import gym.crm.backend.repository.TrainerRepository;
import gym.crm.backend.repository.TrainingTypeRepository;
import gym.crm.backend.util.UserUtil;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class TrainerService {
    private final TrainerRepository trainerRepository;
    private final TrainingTypeRepository trainingTypeRepository;
    private final UserUtil userUtil;

    @Autowired
    public TrainerService(TrainerRepository trainerRepository, TrainingTypeRepository trainingTypeRepository ,UserUtil userUtil) {
        this.trainerRepository = trainerRepository;
        this.trainingTypeRepository = trainingTypeRepository;
        this.userUtil = userUtil;
    }

    @Transactional
    public UserCreationResponse createTrainer(TrainerCreationRequest trainer) {

        String transactionId = MDC.get("transactionId");

        List<String> usernames = getTraineeUsernames(trainerRepository.findAll());

        String username = userUtil.generateUsername(trainer.getFirstName(), trainer.getLastName(), usernames);
        if (username == null || username.isEmpty()) {
            log.error("Transaction ID: {}. Failed to generate username", transactionId);
            throw new UsernameNotCreatedException("Failed to generate username");
        }

        String password = userUtil.generatePassword();
        if (password == null || password.isEmpty()) {
            log.error("Transaction ID: {}. Failed to generate password", transactionId);
            throw new PasswordNotCreatedException("Failed to generate password");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setIsActive(true);
        user.setFirstName(trainer.getFirstName());
        user.setLastName(trainer.getLastName());
        Trainer trainerEntity = new Trainer();
        trainerEntity.setUser(user);

        TrainingType trainingType = trainingTypeRepository.findById(trainer.getTrainingTypeId()).orElseThrow(
                () -> {
                    log.error("Transaction ID: {}. Training type with id: {} not found", transactionId, trainer.getTrainingTypeId());
                    return new TrainingTypeNotFoundException("Training type with id: " + trainer.getTrainingTypeId() + " not found");
                }
        );

        trainerEntity.setSpecialization(trainingType);
        trainerRepository.save(trainerEntity);

        return new UserCreationResponse(username, password);
    }

    public TrainerGetProfileResponse getTrainerByUsername(String username) {
        String transactionId = MDC.get("transactionId");

        Trainer trainer = trainerRepository.findByUserUsername(username).orElseThrow(
                () -> {
                    log.error("Transaction ID: {}. Trainer with username: {} not found", transactionId, username);
                    return new ProfileNotFoundException("Trainer with username: " + username + " not found");
                }
        );

        TrainerGetProfileResponse trainerResponse = new TrainerGetProfileResponse(trainer);

        return trainerResponse;
    }

    public TrainerUpdateResponse updateTrainerProfile(String username, TrainerUpdateRequest trainer) {
        String transactionId = MDC.get("transactionId");

        Trainer trainerEntity = trainerRepository.findByUserUsername(username).orElseThrow(
                () -> {
                    log.error("Transaction ID: {}. Trainer with username: {} not found", transactionId, username);
                    return new ProfileNotFoundException("Trainer with username: " + username + " not found");
                }
        );
        trainerEntity.getUser().setFirstName(trainer.getFirstName());
        trainerEntity.getUser().setLastName(trainer.getLastName());

        TrainingType trainingType = trainingTypeRepository.findById(trainer.getTrainingTypeId()).orElseThrow(
                () -> {
                    log.error("Transaction ID: {}. Training type with id: {} not found", transactionId, trainer.getTrainingTypeId());
                    return new TrainingTypeNotFoundException("Training type with id: " + trainer.getTrainingTypeId() + " not found");
                }
        );

        trainerEntity.setSpecialization(trainingType);
        trainerRepository.save(trainerEntity);

        return new TrainerUpdateResponse(trainerEntity);
    }

    private List<String> getTraineeUsernames(List<Trainer> trainers) {
        if (trainers.isEmpty()) {
            return List.of();
        }
        return trainers.stream().map(t -> t.getUser().getUsername()).toList();
    }
}
