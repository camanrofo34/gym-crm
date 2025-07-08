package gym.crm.backend.service;


import gym.crm.backend.domain.entities.Trainer;
import gym.crm.backend.domain.entities.TrainingType;
import gym.crm.backend.domain.entities.User;
import gym.crm.backend.domain.request.TrainerCreationRequest;
import gym.crm.backend.domain.request.TrainerUpdateRequest;
import gym.crm.backend.domain.response.trainer.TrainerGetProfileResponse;
import gym.crm.backend.domain.response.UserCreationResponse;
import gym.crm.backend.domain.response.trainer.TrainerUpdateResponse;
import gym.crm.backend.exception.types.notFound.ProfileNotFoundException;
import gym.crm.backend.exception.types.notFound.TrainingTypeNotFoundException;
import gym.crm.backend.repository.TrainerRepository;
import gym.crm.backend.repository.TrainingTypeRepository;
import gym.crm.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class TrainerService {
    private final TrainerRepository trainerRepository;
    private final TrainingTypeRepository trainingTypeRepository;
    private final UserRepository userRepository;
    private final UserCredentialService userCredentialService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public TrainerService(TrainerRepository trainerRepository,
                          TrainingTypeRepository trainingTypeRepository,
                          UserRepository userRepository,
                          UserCredentialService userCredentialService,
                          PasswordEncoder passwordEncoder) {
        this.trainerRepository = trainerRepository;
        this.trainingTypeRepository = trainingTypeRepository;
        this.userRepository = userRepository;
        this.userCredentialService = userCredentialService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserCreationResponse createTrainer(TrainerCreationRequest trainer) {
        String transactionId = MDC.get("transactionId");

        String username = userCredentialService.generateUsername(trainer.getFirstName(), trainer.getLastName());

        String originalPassword = userCredentialService.generatePassword();
        String password = passwordEncoder.encode(originalPassword);

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setIsActive(true);
        user.setFirstName(trainer.getFirstName());
        user.setLastName(trainer.getLastName());
        userRepository.save(user);
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

        return new UserCreationResponse(username, originalPassword);
    }

    public TrainerGetProfileResponse getTrainerByUsername(String username) {
        String transactionId = MDC.get("transactionId");

        Trainer trainer = trainerRepository.findByUserUsername(username).orElseThrow(
                () -> {
                    log.error("Transaction ID: {}. Trainer with username: {} not found", transactionId, username);
                    return new ProfileNotFoundException("Trainer with username: " + username + " not found");
                }
        );

        return new TrainerGetProfileResponse(trainer);
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
}
