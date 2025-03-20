package gym.crm.backend.service;


import gym.crm.backend.domain.entities.Trainer;
import gym.crm.backend.domain.entities.User;
import gym.crm.backend.domain.request.TrainerCreationRequest;
import gym.crm.backend.domain.request.TrainerUpdateRequest;
import gym.crm.backend.domain.response.trainer.TrainerGetProfileResponse;
import gym.crm.backend.domain.response.UserCreationResponse;
import gym.crm.backend.domain.response.trainer.TrainerUpdateResponse;
import gym.crm.backend.repository.TrainerRepository;
import gym.crm.backend.repository.TrainingTypeRepository;
import gym.crm.backend.util.UserUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TrainerService {
    private final TrainerRepository trainerRepository;
    private final TrainingTypeRepository trainingTypeRepository;
    private final UserUtil userUtil;

    private final Logger log = LoggerFactory.getLogger(TrainerService.class);

    @Autowired
    public TrainerService(TrainerRepository trainerRepository, TrainingTypeRepository trainingTypeRepository ,UserUtil userUtil) {
        this.trainerRepository = trainerRepository;
        this.trainingTypeRepository = trainingTypeRepository;
        this.userUtil = userUtil;
    }

    public Optional<UserCreationResponse> createTrainer(TrainerCreationRequest trainer) {
        List<String> usernames = getTraineeUsernames(trainerRepository.findAll());
        String username = userUtil.generateUsername(trainer.getFirstName(), trainer.getLastName(), usernames);
        log.info("Transaction ID: {}. Generating username for trainer: {}", MDC.get("transactionId"),username);
        if (username == null) {
            log.error("Transaction ID: {}. Error creating username", MDC.get("transactionId"));
            throw new RuntimeException("Failed to generate username");
        }
        String password = userUtil.generatePassword();
        if (password == null) {
            log.error("Transaction ID: {}. Error creating password", MDC.get("transactionId"));
            throw new RuntimeException("Failed to generate password");
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setIsActive(true);
        user.setFirstName(trainer.getFirstName());
        user.setLastName(trainer.getLastName());
        Trainer trainerEntity = new Trainer();
        trainerEntity.setUser(user);
        trainerEntity.setSpecialization(trainingTypeRepository.getReferenceById(trainer.getTrainingTypeId()));
        trainerRepository.save(trainerEntity);
        log.info("Transaction ID: {}. Successfully created trainer", MDC.get("transactionId"));
        return Optional.of(new UserCreationResponse(username, password));
    }

    public Optional<TrainerGetProfileResponse> getTrainerByUsername(String username) {
        log.info("Transaction ID: {}. Getting trainer profile", MDC.get("transactionId"));
        Trainer trainer = trainerRepository.findByUserUsername(username).orElse(null);
        if (trainer == null) {
            log.error("Transaction ID: {}. Error getting trainer profile", MDC.get("transactionId"));
            return Optional.empty();
        }
        TrainerGetProfileResponse trainerResponse = new TrainerGetProfileResponse(trainer);
        log.info("Transaction ID: {}. Successfully fetched trainer profile", MDC.get("transactionId"));
        return Optional.of(trainerResponse);
    }

    public Optional<TrainerUpdateResponse> updateTrainerProfile(String username, TrainerUpdateRequest trainer) {
        log.info("Transaction ID: {}. Updating trainer profile", MDC.get("transactionId"));
        Trainer trainerEntity = trainerRepository.findByUserUsername(username)
                .orElse(null);
        if (trainerEntity == null) {
            log.error("Transaction ID: {}. Error updating trainer profile", MDC.get("transactionId"));
            return Optional.empty();
        }
        trainerEntity.getUser().setFirstName(trainer.getFirstName());
        trainerEntity.getUser().setLastName(trainer.getLastName());
        trainerEntity.setSpecialization(trainingTypeRepository.getReferenceById(trainer.getTrainingTypeId()));
        trainerRepository.save(trainerEntity);
        log.info("Transaction ID: {}. Successfully updated trainer profile", MDC.get("transactionId"));
        TrainerUpdateResponse trainerUpdateResponse = new TrainerUpdateResponse(trainerEntity);
        return Optional.of(trainerUpdateResponse);
    }

    private List<String> getTraineeUsernames(List<Trainer> trainers) {
        if (trainers.isEmpty()) {
            return List.of();
        }
        return trainers.stream().map(t -> t.getUser().getUsername()).toList();
    }
}
