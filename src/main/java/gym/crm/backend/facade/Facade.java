package gym.crm.backend.facade;

import gym.crm.backend.domain.Trainee;
import gym.crm.backend.domain.Trainer;
import gym.crm.backend.domain.Training;
import gym.crm.backend.service.TraineeService;
import gym.crm.backend.service.TrainerService;
import gym.crm.backend.service.TrainingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class Facade {

    private final TrainingService trainingService;
    private final TrainerService trainerService;
    private final TraineeService traineeService;

    @Autowired
    public Facade(TrainingService trainingService, TrainerService trainerService, TraineeService traineeService) {
        this.trainingService = trainingService;
        this.trainerService = trainerService;
        this.traineeService = traineeService;
    }

    public void run() {
        System.out.println("Facade is running");
    }

    public Trainer createTrainerProfile(Trainer trainer) {
        return trainerService.createTrainer(trainer);
    }

    public Trainee createTraineeProfile(Trainee trainee) {
        return traineeService.createTrainee(trainee);
    }

    public boolean matchTrainerUsernameAndPassword(String username, String password) {
        return trainerService.matchTrainerUsernameAndPassword(username, password);
    }

    public boolean matchTraineeUsernameAndPassword(String username, String password) {
        return traineeService.matchTraineeUsernameAndPassword(username, password);
    }

    //Requires previous authentication

    public Trainer selectTrainerProfileByUsername(String username, String password, String usernameToSearch) {
        if (matchTrainerUsernameAndPassword(username, password)) {
            return trainerService.getTrainerByUsername(usernameToSearch).orElse(null);
        }
        return null;
    }

    public Trainee selectTraineeProfileByUsername(String username, String password, String usernameToSearch) {
        if (matchTraineeUsernameAndPassword(username, password)) {
            return traineeService.getTraineeByUsername(usernameToSearch).orElse(null);
        }
        return null;
    }

    public void changeTrainerPassword(String username, String password, String newPassword) {
        if (matchTrainerUsernameAndPassword(username, password)) {
            trainerService.changePassword(username, newPassword);
        }
    }

    public void changeTraineePassword(String username, String password, String newPassword) {
        if (matchTraineeUsernameAndPassword(username, password)) {
            traineeService.changePassword(username, newPassword);
        }
    }

    public void updateTrainerProfile(Trainer trainer, String username, String password) {
        if (matchTrainerUsernameAndPassword(username, password)) {
            trainerService.updateTrainerProfile(trainer);
        }
    }

    public void updateTraineeProfile(Trainee trainee, String username, String password) {
        if (matchTraineeUsernameAndPassword(username, password)) {
            traineeService.updateTraineeProfile(trainee);
        }
    }

    public boolean activateDeactivateTrainer(String username, String password, String usernameToChange) {
        if (matchTrainerUsernameAndPassword(username, password)) {
            trainerService.activateDeactivateTrainer(usernameToChange);
            return true;
        }
        return false;
    }

    public boolean activateDeactivateTrainee(String username, String password, String usernameToChange) {
        if (matchTraineeUsernameAndPassword(username, password)) {
            traineeService.activateDeactivateTrainee(usernameToChange);
            return true;
        }
        return false;
    }

    public boolean deleteTraineeByUsername(String username, String password, String usernameToDelete) {
        if (matchTraineeUsernameAndPassword(username, password)) {
            traineeService.deleteTrainee(usernameToDelete);
            return true;
        }
        return false;
    }

    public List<Training> getTraineeTrainingsByTraineeUsernameFromDateToDateTrainerNameTrainingType(String traineeUsername, Date fromDate, Date toDate, String trainerName, String trainingType) {
        return trainingService.getTraineeTrainings(traineeUsername, fromDate, toDate, trainerName, trainingType);
    }

    public List<Training> getTrainerTrainingsByTrainerUsernameFromDateToDateTraineeName(String trainerUsername, Date fromDate, Date toDate, String traineeName) {
        return trainingService.getTrainerTrainings(trainerUsername, fromDate, toDate, traineeName);
    }

    public Training createTraining(Training training, String username, String password) {
        if (matchTrainerUsernameAndPassword(username, password)) {
            return trainingService.createTraining(training);
        }
        return null;
    }

    public List<Trainer> getTrainersNotInTrainersTraineeListByTraineeUserUsername(String traineeUsername, String password) {
        if (matchTraineeUsernameAndPassword(traineeUsername, password)) {
            return traineeService.getTrainersNotInTrainersTraineeListByTraineeUserUsername(traineeUsername);
        }
        return null;
    }

    public boolean updateTrainersTraineeList(String traineeUsername, String traineePassword, String trainerUsername) {
        if (matchTraineeUsernameAndPassword(traineeUsername, traineePassword)) {
            return traineeService.updateTrainersTraineeList(traineeUsername, trainerUsername);
        }
        return false;
    }
}
