package gym.crm.backend.service;


import gym.crm.backend.domain.Trainer;
import gym.crm.backend.domain.Training;
import gym.crm.backend.repository.TrainerRepository;
import gym.crm.backend.repository.TrainingRepository;
import gym.crm.backend.util.UserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class TrainerService {
    private final TrainerRepository trainerRepository;
    private final TrainingRepository trainingRepository;

    @Autowired
    public TrainerService(TrainerRepository trainerRepository, TrainingRepository trainingRepository) {
        this.trainerRepository = trainerRepository;
        this.trainingRepository = trainingRepository;
    }

    public Trainer createTrainer(Trainer trainer) {
        List<String> usernames = trainerRepository.findAll().stream().map(t -> t.getUser().getUsername()).toList();
        String username = UserUtil.generateUsername(trainer.getUser().getFirstName(), trainer.getUser().getLastName(), usernames);
        trainer.getUser().setUsername(username);
        String password = UserUtil.generatePassword();
        trainer.getUser().setPassword(password);
        return trainerRepository.save(trainer);
    }

    public Optional<Trainer> getTrainerByUsername(String username) {
        return trainerRepository.findByUserUsername(username);
    }

    public boolean matchTrainerUsernameAndPassword(String username, String password) {
        Optional<Trainer> trainer = trainerRepository.findByUserUsername(username);
        return trainer.map(t -> t.getUser().getPassword().equals(password)).orElse(false);
    }

    public void changePassword(String username, String newPassword) {
        Optional<Trainer> trainer = trainerRepository.findByUserUsername(username);
        trainer.ifPresent(t -> {
            t.getUser().setPassword(newPassword);
            trainerRepository.save(t);
        });
    }

    public Trainer updateTrainerProfile(Trainer trainer) {
        return trainerRepository.save(trainer);
    }

    public void activateDeactivateTrainer(String username, boolean status) {
        Optional<Trainer> trainer = trainerRepository.findByUserUsername(username);
        trainer.ifPresent(t -> {
            t.getUser().setIsActive(status);
            trainerRepository.save(t);
        });
    }

    public List<Training> getTrainerTrainings(String username, Date fromDate, Date toDate, String traineeName) {
        return trainingRepository.findTrainerTrainings(username, fromDate, toDate, traineeName);
    }

    public List<Trainer> getTrainersNotInTrainersTraineeListByTraineeUserUsername(String traineeUsername) {
        return trainerRepository.findTrainersNotInTrainersTraineeListByTraineeUserUsername(traineeUsername);
    }
}
