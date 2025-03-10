package gym.crm.backend.service;

import gym.crm.backend.domain.Trainee;
import gym.crm.backend.domain.Trainer;
import gym.crm.backend.repository.TraineeRepository;
import gym.crm.backend.repository.TrainerRepository;
import gym.crm.backend.util.UserUtil;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class TraineeService {

    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final UserUtil userUtil;

    private final Logger log = Logger.getLogger(TraineeService.class.getName());

    @Autowired
    public TraineeService(TraineeRepository traineeRepository, TrainerRepository trainerRepository, UserUtil userUtil) {
        this.traineeRepository = traineeRepository;
        this.trainerRepository = trainerRepository;
        this.userUtil = userUtil;
    }

    public Optional<Trainee> createTrainee(Trainee trainee) {
        if (!dataValidation(trainee)) {
            log.log(Level.WARNING, "Trainee data validation failure");
            return Optional.empty();
        }
        List<String> usernames = getTraineeUsernames(traineeRepository.findAll());
        String username = userUtil.generateUsername(trainee.getUser().getFirstName(), trainee.getUser().getLastName(), usernames);
        if (username == null) {
            log.severe("Username generation failed!");
            throw new RuntimeException("Failed to generate username");
        }
        trainee.getUser().setUsername(username);
        String password = userUtil.generatePassword();
        if (password == null) {
            log.severe("Password generation failed!");
            throw new RuntimeException("Failed to generate password");
        }
        trainee.getUser().setPassword(password);
        log.log(Level.INFO, "Trainee created with username: {0}", username);
        return Optional.of(traineeRepository.save(trainee));
    }

    public Optional<Trainee> getTraineeByUsername(String username) {
        log.log(Level.INFO, "Getting trainee by username: {0}", username);
        return traineeRepository.findByUserUsername(username);
    }

    public boolean matchTraineeUsernameAndPassword(String username, String password) {
        Optional<Trainee> trainee = traineeRepository.findByUserUsername(username);
        log.log(Level.INFO, "Matching trainee by username: {0}", username);
        return trainee.map(t -> t.getUser().getPassword().equals(password)).orElse(false);
    }

    public void changePassword(String username, String newPassword) {
        Optional<Trainee> trainee = traineeRepository.findByUserUsername(username);
        log.log(Level.INFO, "Changing trainee password by username: {0}", username);
        trainee.ifPresent(t -> {
            t.getUser().setPassword(newPassword);
            traineeRepository.save(t);
        });
    }

    public void updateTraineeProfile(Trainee trainee) {
        if (dataValidation(trainee)) {
            log.log(Level.INFO, "Updating trainee profile");
            traineeRepository.save(trainee);
        }else {
            log.log(Level.WARNING, "Trainee data validation failure");
        }
    }

    public void activateDeactivateTrainee(String username) {
        Optional<Trainee> trainee = traineeRepository.findByUserUsername(username);
        trainee.ifPresent(t -> {
            t.getUser().setIsActive(!t.getUser().getIsActive());
            traineeRepository.save(t);
        });
        log.log(Level.INFO, "Trainee changed state by username: {0}", username);
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
        log.log(Level.INFO, "Trainee deleted by username: {0}", username);
        traineeRepository.delete(trainee);
    }

    public List<Trainer> getTrainersNotInTrainersTraineeListByTraineeUserUsername(String traineeUsername) {
        log.log(Level.INFO, "Getting trainers by username: {0}", traineeUsername);
        return traineeRepository.findTrainersNotInTrainersTraineeListByTraineeUserUsername(traineeUsername);
    }

    public boolean updateTrainersTraineeList(String username, String trainerUsername) {
        Trainee trainee = traineeRepository.findByUserUsername(username).orElse(null);
        Trainer trainer = trainerRepository.findByUserUsername(trainerUsername).orElse(null);
        if (trainee == null || trainer == null) {
            return false;
        }
        if (trainee.getTrainers() == null) {
            trainee.setTrainers(new HashSet<>());
        }
        if (!trainee.getTrainers().contains(trainer)) {
            trainee.getTrainers().add(trainer);
            traineeRepository.save(trainee);
            log.log(Level.INFO, "Trainer {0} added to Trainee {1}", new Object[]{trainerUsername, username});
            return true;
        }
        return false;
    }


    private boolean dataValidation(Trainee trainee) {
        log.log(Level.INFO, "Checking data validation");
        try {
            return trainee.getUser().getFirstName() != null && trainee.getUser().getLastName() != null;
        } catch (NullPointerException e) {
            return false;
        }
    }

    private List<String> getTraineeUsernames(List<Trainee> trainees) {
        if (trainees.isEmpty()) {
            return List.of();
        }
        return trainees.stream().map(t -> t.getUser().getUsername()).toList();
    }
}
