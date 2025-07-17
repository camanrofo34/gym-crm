package gym.crm.backend.service;

import gym.crm.backend.domain.entities.*;
import gym.crm.backend.domain.request.TraineeCreationRequest;
import gym.crm.backend.domain.request.TraineeUpdateRequest;
import gym.crm.backend.domain.request.TrainerWorkloadRequest;
import gym.crm.backend.domain.request.TrainingCreationRequest;
import gym.crm.backend.domain.response.trainee.TraineeGetProfileResponse;
import gym.crm.backend.domain.response.trainee.TraineeUpdateResponse;
import gym.crm.backend.domain.response.UserCreationResponse;
import gym.crm.backend.domain.response.trainee.TrainersTraineeResponse;
import gym.crm.backend.exception.types.notFound.ProfileNotFoundException;
import gym.crm.backend.integration.feign.TrainerWorkloadClient;
import gym.crm.backend.messaging.producer.TrainerWorkloadMessageProducer;
import gym.crm.backend.repository.TraineeRepository;
import gym.crm.backend.repository.TrainerRepository;
import gym.crm.backend.repository.TrainingRepository;
import gym.crm.backend.repository.UserRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.*;

@Service
@Slf4j
public class TraineeService {

    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final TrainingRepository trainingRepository;
    private final UserRepository userRepository;
    private final UserCredentialService userCredentialService;
    private final PasswordEncoder passwordEncoder;
    private final TrainerWorkloadMessageProducer trainerWorkloadMessageProducer;

    @Autowired
    public TraineeService(TraineeRepository traineeRepository,
                          TrainerRepository trainerRepository,
                          UserRepository userRepository,
                          UserCredentialService userCredentialService,
                          PasswordEncoder passwordEncoder,
                          TrainingRepository trainingRepository,
                          TrainerWorkloadMessageProducer trainerWorkloadMessageProducer) {
        this.traineeRepository = traineeRepository;
        this.trainerRepository = trainerRepository;
        this.userRepository = userRepository;
        this.userCredentialService = userCredentialService;
        this.passwordEncoder = passwordEncoder;
        this.trainingRepository = trainingRepository;
        this.trainerWorkloadMessageProducer = trainerWorkloadMessageProducer;
    }

    @Transactional
    public UserCreationResponse createTrainee(TraineeCreationRequest trainee) {
        String username = userCredentialService.generateUsername(trainee.getFirstName(), trainee.getLastName());
        String originalPassword = userCredentialService.generatePassword();
        String password = passwordEncoder.encode(originalPassword);
        Trainee traineeEntity = getTrainee(trainee, username, password);
        traineeRepository.save(traineeEntity);
        return new UserCreationResponse(username, originalPassword);
    }

    public TraineeGetProfileResponse getTraineeByUsername(String username) {
        String transactionId = MDC.get("transactionId");

        Trainee trainee = traineeRepository.findByUserUsername(username).orElseThrow(() ->
        {
            log.error("Transaction ID: {}. Trainee with username: {} not found", transactionId, username);
            return new ProfileNotFoundException("Trainee with username: " + username + " not found");
        });
        return new TraineeGetProfileResponse(trainee);
    }

    public TraineeUpdateResponse updateTrainee(String username, TraineeUpdateRequest trainee) {
        String transactionId = MDC.get("transactionId");

        Trainee traineeEntity = traineeRepository.findByUserUsername(username).orElseThrow(() ->
        {
            log.error("Transaction ID: {}. Trainee with username: {} not found", transactionId, username);
            return new ProfileNotFoundException("Trainee with username: " + username + " not found");
        });

        traineeEntity.getUser().setFirstName(trainee.getFirstName());
        traineeEntity.getUser().setLastName(trainee.getLastName());

        if (trainee.getDateOfBirth() != null) {
            traineeEntity.setDateOfBirth(trainee.getDateOfBirth());
        }
        if (!trainee.getAddress().isEmpty()) {
            traineeEntity.setAddress(trainee.getAddress());
        }

        traineeRepository.save(traineeEntity);
        return new  TraineeUpdateResponse(traineeEntity);
    }

    @Transactional
    @CircuitBreaker(name = "trainerWorkloadClient", fallbackMethod = "createTrainingFallback")
    public void deleteTrainee(String username) {
        String transactionId = MDC.get("transactionId");

        Trainee trainee = traineeRepository.findByUserUsername(username)
                .orElseThrow(() -> {
                    log.error("Transaction ID: {}. Trainee with username: {} not found", transactionId, username);
                    return new ProfileNotFoundException("Trainee with username: " + username + " not found");
                });

        List<Training> trainings = trainingRepository.findTrainingsByTrainee(trainee);

        for (Training training : trainings) {
            Trainer trainer = training.getTrainer();
            TrainerWorkloadRequest request = new TrainerWorkloadRequest(
                    trainer.getUser().getUsername(),
                    trainer.getUser().getFirstName(),
                    trainer.getUser().getLastName(),
                    trainer.getUser().getIsActive(),
                    training.getTrainingDate(),
                    training.getTrainingDuration(),
                    ActionType.DELETE
            );
            trainerWorkloadMessageProducer.sendTrainerWorkloadRequest(request, transactionId, getCurrentBearerToken());
        }

        for (Trainer trainer : trainee.getTrainers()) {
            trainer.getTrainees().remove(trainee);
        }

        traineeRepository.delete(trainee);
    }

    public Page<TrainersTraineeResponse> getTrainersNotInTrainersTraineeListByTraineeUserUsername(String traineeUsername, Pageable pageable) {
        String transactionId = MDC.get("transactionId");

        Trainee trainee = traineeRepository.findByUserUsername(traineeUsername)
                .orElseThrow(() -> {
                    log.error("Transaction ID: {}. Trainee with username: {} not found", transactionId, traineeUsername);
                    return new ProfileNotFoundException("Trainee with username: " + traineeUsername + " not found");
                });

        Page<Trainer> trainersFound = traineeRepository.findTrainersNotInTrainersTraineeListByTraineeUserUsername(traineeUsername, pageable);

        return trainersFound.map(
                trainer -> new TrainersTraineeResponse(
                        trainer.getUser().getUsername(),
                        trainer.getUser().getFirstName(),
                        trainer.getUser().getLastName(),
                        trainer.getSpecialization().getId()
                )
        );
    }

    public Set<TrainersTraineeResponse> updateTrainersTraineeList(String username, List<String> trainerUsername) {

        String transactionId = MDC.get("transactionId");


        Trainee trainee = traineeRepository.findByUserUsername(username).orElseThrow(() ->{
                    log.error("Transaction ID: {}. Trainee with username: {} not found", transactionId, username);
                    return new ProfileNotFoundException("Trainee with username: " + username + " not found");
                }
        );

        List<Trainer> trainers = trainerRepository.findAll();
        Set<TrainersTraineeResponse> trainersTraineeResponses = new HashSet<>();
        Set<Trainer> newTrainers = new HashSet<>();

        for (String trainer : trainerUsername) {
            Trainer trainerEntity = trainers.stream().filter(t -> t.getUser().getUsername().equals(trainer)).findFirst().orElse(null);
            if (trainerEntity != null) {
                newTrainers.add(trainerEntity);
                Set<Trainee> trainees = trainerEntity.getTrainees();
                trainees.add(trainee);
                trainerEntity.setTrainees(trainees);
                trainerRepository.save(trainerEntity);
                TrainersTraineeResponse trainersTraineeResponse = new TrainersTraineeResponse(
                        trainerEntity.getUser().getUsername(),
                        trainerEntity.getUser().getFirstName(),
                        trainerEntity.getUser().getLastName(),
                        trainerEntity.getSpecialization().getId()
                );
                trainersTraineeResponses.add(trainersTraineeResponse);
            }
        }
        trainee.setTrainers(newTrainers);
        traineeRepository.save(trainee);
        return trainersTraineeResponses;
    }

    private Trainee getTrainee(TraineeCreationRequest trainee, String username, String password) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setFirstName(trainee.getFirstName());
        user.setLastName(trainee.getLastName());
        user.setIsActive(true);
        userRepository.save(user);
        Trainee traineeEntity = new Trainee();
        traineeEntity.setUser(user);
        if (trainee.getDateOfBirth() != null) {
            traineeEntity.setDateOfBirth(trainee.getDateOfBirth());
        }
        if (!trainee.getAddress().isEmpty()) {
            traineeEntity.setAddress(trainee.getAddress());
        }
        return traineeEntity;
    }

    private String getCurrentBearerToken() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) return null;

        HttpServletRequest request = attributes.getRequest();
        String authHeader = request.getHeader("Authorization");
        return (authHeader != null && authHeader.startsWith("Bearer "))
                ? authHeader
                : null;
    }

    public void createTrainingFallback(TrainingCreationRequest training, Throwable throwable) {
        String transactionId = MDC.get("transactionId");
        log.error("Transaction ID: {}. Failed to create training due to: {}", transactionId, throwable.getMessage());
        throw new RuntimeException("Failed to create training. Please try again later.");
    }

}
