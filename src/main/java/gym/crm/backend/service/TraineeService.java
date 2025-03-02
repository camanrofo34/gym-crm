package gym.crm.backend.service;

import gym.crm.backend.domain.Trainee;
import gym.crm.backend.domain.Trainer;
import gym.crm.backend.domain.Training;
import gym.crm.backend.repository.TraineeRepository;
import gym.crm.backend.repository.TrainingRepository;
import gym.crm.backend.util.UserUtil;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class TraineeService {

    private final TraineeRepository traineeRepository;
    private final TrainingRepository trainingRepository;

    @Autowired
    public TraineeService(TraineeRepository traineeRepository, TrainingRepository trainingRepository) {
        this.traineeRepository = traineeRepository;
        this.trainingRepository = trainingRepository;
    }

    public Trainee createTrainee(Trainee trainee) {
        List<String> usernames = traineeRepository.findAll().stream().map(t -> t.getUser().getUsername()).toList();
        String username = UserUtil.generateUsername(trainee.getUser().getFirstName(), trainee.getUser().getLastName(), usernames);
        trainee.getUser().setUsername(username);
        String password = UserUtil.generatePassword();
        trainee.getUser().setPassword(password);
        return traineeRepository.save(trainee);
    }

    public Optional<Trainee> getTraineeByUsername(String username) {
        return traineeRepository.findByUserUsername(username);
    }

    public boolean matchTraineeUsernameAndPassword(String username, String password) {
        Optional<Trainee> trainee = traineeRepository.findByUserUsername(username);
        return trainee.map(t -> t.getUser().getPassword().equals(password)).orElse(false);
    }

    public void changePassword(String username, String newPassword) {
        Optional<Trainee> trainee = traineeRepository.findByUserUsername(username);
        trainee.ifPresent(t -> {
            t.getUser().setPassword(newPassword);
            traineeRepository.save(t);
        });
    }

    public Trainee updateTraineeProfile(Trainee trainee) {
        return traineeRepository.save(trainee);
    }

    public void activateDeactivateTrainee(String username, boolean status) {
        Optional<Trainee> trainee = traineeRepository.findByUserUsername(username);
        trainee.ifPresent(t -> {
            t.getUser().setIsActive(status);
            traineeRepository.save(t);
        });
    }

    @Transactional
    public void deleteTrainee(String username) {
        Optional<Trainee> trainee = traineeRepository.findByUserUsername(username);
        trainee.ifPresent(t -> {
            for (Trainer trainer : t.getTrainers()) {
                trainer.getTrainees().remove(t);
            }
            traineeRepository.delete(t);
        });
    }

    public List<Training> getTraineeTrainings(String username, Date fromDate, Date toDate, String trainerName, String trainingType) {
        return trainingRepository.findTraineeTrainings(username, fromDate, toDate, trainerName, trainingType);
    }

//    public void updateTrainersTraineeList(String username, Long trainerId) {
//        traineeRepository.updateTrainersTraineeList(username, trainerId);
//    }
}
