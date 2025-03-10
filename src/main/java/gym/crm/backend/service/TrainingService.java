package gym.crm.backend.service;

import gym.crm.backend.domain.Training;
import gym.crm.backend.repository.TrainingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class TrainingService {

    private final TrainingRepository trainingRepository;

    private final Logger log = Logger.getLogger(TrainingService.class.getName());

    @Autowired
    public TrainingService(TrainingRepository trainingRepository) {
        this.trainingRepository = trainingRepository;
    }

    public List<Training> getTrainerTrainings(String username, Date fromDate, Date toDate, String traineeName) {
        log.log(Level.INFO, "Getting trainer's training list by the criteria, username: {0}, fromDate: {1}, toDate: {2}, traineeName: {3}", new Object[]{username, fromDate, toDate, traineeName});
        return trainingRepository.findTrainerTrainings(username, fromDate, toDate, traineeName);
    }

    public List<Training> getTraineeTrainings(String username, Date fromDate, Date toDate, String trainerName, String trainingType) {
        log.log(Level.INFO, "Getting trainee's training list by the criteria, username: {0}, fromDate: {1}, toDate: {2}, trainerName: {3}, trainingType: {4}", new Object[]{username, fromDate, toDate, trainerName, trainingType});
        return trainingRepository.findTraineeTrainings(username, fromDate, toDate, trainerName, trainingType);
    }

    public Optional<Training> createTraining(Training training) {
        if (training.getTrainingName() == null) {
            log.log(Level.WARNING, "Training name is empty");
            return Optional.empty();
        }
        if (training.getTrainingDate() == null) {
            log.log(Level.WARNING, "Training date is empty");
            return Optional.empty();
        }
        if (training.getTrainingDuration() == null) {
            log.log(Level.WARNING, "Training duration is empty");
            return Optional.empty();
        }
        return Optional.of(trainingRepository.save(training));
    }
}
