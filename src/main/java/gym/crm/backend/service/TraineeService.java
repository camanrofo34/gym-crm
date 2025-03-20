package gym.crm.backend.service;

import gym.crm.backend.domain.entities.Trainee;
import gym.crm.backend.domain.entities.Trainer;
import gym.crm.backend.domain.entities.User;
import gym.crm.backend.domain.request.TraineeCreationRequest;
import gym.crm.backend.domain.request.TraineeUpdateRequest;
import gym.crm.backend.domain.response.trainee.TraineeGetProfileResponse;
import gym.crm.backend.domain.response.trainee.TraineeUpdateResponse;
import gym.crm.backend.domain.response.UserCreationResponse;
import gym.crm.backend.domain.response.trainee.TrainersTraineeResponse;
import gym.crm.backend.repository.TraineeRepository;
import gym.crm.backend.repository.TrainerRepository;
import gym.crm.backend.util.UserUtil;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TraineeService {

    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final UserUtil userUtil;

    private final Logger log = LoggerFactory.getLogger(TraineeService.class);

    @Autowired
    public TraineeService(TraineeRepository traineeRepository, TrainerRepository trainerRepository, UserUtil userUtil) {
        this.traineeRepository = traineeRepository;
        this.trainerRepository = trainerRepository;
        this.userUtil = userUtil;
    }

    public Optional<UserCreationResponse> createTrainee(TraineeCreationRequest trainee) {
        List<String> usernames = getTraineeUsernames(traineeRepository.findAll());
        log.info("Transaction ID: {}. Creating trainee", MDC.get("transactionId"));
        String username = userUtil.generateUsername(trainee.getFirstName(), trainee.getLastName(), usernames);
        if (username.isBlank()) {
            log.error("Transaction ID: {}. Error creting username", MDC.get("transactionId"));
            throw new RuntimeException("Failed to generate username");
        }
        String password = userUtil.generatePassword();
        if (password == null) {
            log.error("Transaction ID: {}. Error creating password", MDC.get("transactionId"));
            throw new RuntimeException("Failed to generate password");
        }
        Trainee traineeEntity = getTrainee(trainee, username, password);
        traineeRepository.save(traineeEntity);
        log.info("Transaction ID: {}. Created trainee", MDC.get("transactionId"));
        return Optional.of(new UserCreationResponse(username, password));
    }

    public Optional<TraineeGetProfileResponse> getTraineeByUsername(String username) {
        log.info("Transaction ID: {}. Getting trainee profile", MDC.get("transactionId"));
        Trainee trainee = traineeRepository.findByUserUsername(username).orElse(null);
        if (trainee == null) {
            log.error("Transaction ID: {}. Error getting trainee", MDC.get("transactionId"));
            return Optional.empty();
        }
        TraineeGetProfileResponse traineeCreationResponse = new TraineeGetProfileResponse(trainee);
        return Optional.of(traineeCreationResponse);
    }

    public Optional<TraineeUpdateResponse> updateTrainee(String username, TraineeUpdateRequest trainee) {
        log.info("Transaction ID: {}. Updating trainee", MDC.get("transactionId"));
        Trainee traineeEntity = traineeRepository.findByUserUsername(username)
                .orElse(null);
        if (traineeEntity == null) {
            log.error("Transaction ID: {}. Error updating trainee", MDC.get("transactionId"));
            return Optional.empty();
        }
        traineeEntity.getUser().setFirstName(trainee.getFirstName());
        traineeEntity.getUser().setLastName(trainee.getLastName());
        if (trainee.getDateOfBirth() != null) {
            traineeEntity.setDateOfBirth(trainee.getDateOfBirth());
        }
        if (!trainee.getAddress().isEmpty()) {
            traineeEntity.setAddress(trainee.getAddress());
        }
        traineeRepository.save(traineeEntity);
        log.info("Transaction ID: {}. Updated trainee", MDC.get("transactionId"));
        TraineeUpdateResponse traineeUpdateResponse = new  TraineeUpdateResponse(traineeEntity);
        return Optional.of(traineeUpdateResponse);
    }

    @Transactional
    public void deleteTrainee(String username) {
        Trainee trainee = traineeRepository.findByUserUsername(username)
                .orElseThrow(() -> new RuntimeException("Trainee not found with username: " + username));
        if (trainee.getTrainers() != null) {
            for (Trainer trainer : trainee.getTrainers()) {
                trainer.getTrainees().remove(trainee);
            }
        }
        log.info("Transaction ID: {}. Deleting trainee", MDC.get("transactionId"));
        traineeRepository.delete(trainee);
    }

    public List<TrainersTraineeResponse> getTrainersNotInTrainersTraineeListByTraineeUserUsername(String traineeUsername) {
        log.info("Transaction ID: {}. Getting trainers not in trainee list", MDC.get("transactionId"));
        List<TrainersTraineeResponse> trainers = new ArrayList<>();
        List<Trainer> trainersFound =traineeRepository.findTrainersNotInTrainersTraineeListByTraineeUserUsername(traineeUsername);
        trainersFound.forEach(trainer -> {
            TrainersTraineeResponse trainersTraineeResponse = new TrainersTraineeResponse(
                    trainer.getUser().getUsername(),
                    trainer.getUser().getFirstName(),
                    trainer.getUser().getLastName(),
                    trainer.getSpecialization().getId()
            );
            trainers.add(trainersTraineeResponse);
        });
        return trainers;
    }

    public List<TrainersTraineeResponse> updateTrainersTraineeList(String username, List<String> trainerUsername) {
        log.info("Transaction ID: {}. Updating trainers trainee list", MDC.get("transactionId"));
        Trainee trainee = traineeRepository.findByUserUsername(username).orElse(null);
        if (trainee == null) {
            log.error("Transaction ID: {}. Error updating trainers trainee list", MDC.get("transactionId"));
            return List.of();
        }
        List<Trainer> trainers = trainerRepository.findAll();
        List<TrainersTraineeResponse> trainersTraineeResponses = new ArrayList<>();
        List<Trainer> newTrainers = new ArrayList<>();
        for (String trainer : trainerUsername) {
            Trainer trainerEntity = trainers.stream().filter(t -> t.getUser().getUsername().equals(trainer)).findFirst().orElse(null);
            if (trainerEntity != null) {
                newTrainers.add(trainerEntity);
                List<Trainee> trainees = trainerEntity.getTrainees();
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
        log.info("Transaction ID: {}. Updated trainers trainee list", MDC.get("transactionId"));
        return trainersTraineeResponses;
    }

    //Helping methods for working with Trainee entity

    private List<String> getTraineeUsernames(List<Trainee> trainees) {
        if (trainees.isEmpty()) {
            return List.of();
        }
        return trainees.stream().map(t -> t.getUser().getUsername()).toList();
    }

    private Trainee getTrainee(TraineeCreationRequest trainee, String username, String password) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setFirstName(trainee.getFirstName());
        user.setLastName(trainee.getLastName());
        user.setIsActive(true);
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

}
