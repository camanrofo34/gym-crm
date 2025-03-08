package gym.crm.backend.service;


import gym.crm.backend.domain.Trainer;
import gym.crm.backend.repository.TrainerRepository;
import gym.crm.backend.util.UserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class TrainerService {
    private final TrainerRepository trainerRepository;
    private final UserUtil userUtil;

    private final Logger log = Logger.getLogger(TrainerService.class.getName());

    @Autowired
    public TrainerService(TrainerRepository trainerRepository, UserUtil userUtil) {
        this.trainerRepository = trainerRepository;
        this.userUtil = userUtil;
    }

    public Trainer createTrainer(Trainer trainer) {
        if (validateTrainer(trainer)) {
            log.log(Level.WARNING, "Trainer data validation failure");
            return null;
        }
        List<String> usernames = getTraineeUsernames(trainerRepository.findAll());
        String username = userUtil.generateUsername(trainer.getUser().getFirstName(), trainer.getUser().getLastName(), usernames);
        if (username == null) {
            log.severe("Username generation failed!");
            throw new RuntimeException("Failed to generate username");
        }
        trainer.getUser().setUsername(username);
        String password = userUtil.generatePassword();
        if (password == null) {
            log.severe("Password generation failed!");
            throw new RuntimeException("Failed to generate password");
        }
        trainer.getUser().setPassword(password);
        log.log(Level.INFO, "Trainer created with username: {0}", username);
        return trainerRepository.save(trainer);
    }

    public Optional<Trainer> getTrainerByUsername(String username) {
        log.log(Level.INFO, "Getting trainer by username: {0}", username);
        return trainerRepository.findByUserUsername(username);
    }

    public boolean matchTrainerUsernameAndPassword(String username, String password) {
        Optional<Trainer> trainer = trainerRepository.findByUserUsername(username);
        log.log(Level.INFO, "Matching trainer by username: {0}", username);
        return trainer.map(t -> t.getUser().getPassword().equals(password)).orElse(false);
    }

    public void changePassword(String username, String newPassword) {
        Optional<Trainer> trainer = trainerRepository.findByUserUsername(username);
        log.log(Level.INFO, "Changing trainer password by username: {0}", username);
        trainer.ifPresent(t -> {
            t.getUser().setPassword(newPassword);
            trainerRepository.save(t);
        });
    }

    public void updateTrainerProfile(Trainer trainer) {
        if (validateTrainer(trainer)) {
            log.log(Level.WARNING, "Trainer data validation failure");
        }else{
            log.log(Level.INFO, "Updating trainer profile");
            trainerRepository.save(trainer);
        }
    }

    public void activateDeactivateTrainer(String username) {
        Optional<Trainer> trainer = trainerRepository.findByUserUsername(username);
        log.log(Level.INFO, "Activating/Deactivating trainer by username: {0}", username);
        trainer.ifPresent(t -> {
            t.getUser().setIsActive(!t.getUser().getIsActive());
            trainerRepository.save(t);
        });
    }

    public boolean validateTrainer(Trainer trainer) {
        log.log(Level.INFO, "Checking data validation");
        try {
            return trainer.getUser().getFirstName() == null || trainer.getUser().getLastName() == null;
        } catch (NullPointerException e) {
            return true;
        }
    }

    private List<String> getTraineeUsernames(List<Trainer> trainers) {
        if (trainers == null) {
            return List.of();
        }
        return trainers.stream().map(t -> t.getUser().getUsername()).toList();
    }
}
