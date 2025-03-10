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
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class Facade {

    private final TrainingService trainingService;
    private final TrainerService trainerService;
    private final TraineeService traineeService;

    private final Logger log = Logger.getLogger(Facade.class.getName());

    @Autowired
    public Facade(TrainingService trainingService, TrainerService trainerService, TraineeService traineeService) {
        this.trainingService = trainingService;
        this.trainerService = trainerService;
        this.traineeService = traineeService;
    }

    public void run() {
        System.out.println("Facade is running");
    }

    public Optional<Trainer> createTrainerProfile(Trainer trainer) {
        try {
            log.log(Level.INFO, "Creating Trainer Profile {0}", trainer.getUser().getFirstName());
            return trainerService.createTrainer(trainer);
        }catch (Exception e){
            log.log(Level.SEVERE, "Error creating Trainer Profile {0}", e.getMessage());
            return Optional.empty();
        }

    }

    public Optional<Trainee> createTraineeProfile(Trainee trainee) {
        try {
            log.log(Level.INFO, "Creating Trainee Profile {0}", trainee.getUser().getFirstName());
            return traineeService.createTrainee(trainee);
        } catch (Exception e){
            log.log(Level.SEVERE, "Error creating Trainee Profile {0}", e.getMessage());
            return Optional.empty();
        }
    }

    public boolean matchTrainerUsernameAndPassword(String username, String password) {
        return trainerService.matchTrainerUsernameAndPassword(username, password);
    }

    public boolean matchTraineeUsernameAndPassword(String username, String password) {
        return traineeService.matchTraineeUsernameAndPassword(username, password);
    }

    //Requires previous authentication

    public Optional<Trainer> selectTrainerProfileByUsername(String username, String password, String usernameToSearch) {
        try {
            if (matchTrainerUsernameAndPassword(username, password)) {
                log.log(Level.INFO, "Selecting Trainer Profile {0}", usernameToSearch);
                return trainerService.getTrainerByUsername(usernameToSearch);
            }
            log.log(Level.WARNING, "Trainer username and password do not match");
            return Optional.empty();
        }catch (Exception e){
            log.log(Level.SEVERE, "Error selecting Trainer Profile {0}", e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<Trainee> selectTraineeProfileByUsername(String username, String password, String usernameToSearch) {
        try {
            if (matchTraineeUsernameAndPassword(username, password)) {
                log.log(Level.INFO, "Selecting Trainee Profile {0}", usernameToSearch);
                return traineeService.getTraineeByUsername(usernameToSearch);
            }
            log.log(Level.WARNING, "Trainee username and password do not match");
            return Optional.empty();
        }catch (Exception e){
            log.log(Level.SEVERE, "Error selecting Trainee Profile {0}", e.getMessage());
            return Optional.empty();
        }
    }

    public void changeTrainerPassword(String username, String password, String newPassword) {
        try {
            if (matchTrainerUsernameAndPassword(username, password)) {
                log.log(Level.INFO, "Changing Trainer Password {0}", username);
                trainerService.changePassword(username, newPassword);
            }
        }catch (Exception e){
            log.log(Level.SEVERE, "Error changing Trainer Password {0}", e.getMessage());
        }
    }

    public void changeTraineePassword(String username, String password, String newPassword) {
        try {
            if (matchTraineeUsernameAndPassword(username, password)) {
                log.log(Level.INFO, "Changing Trainee Password {0}", username);
                traineeService.changePassword(username, newPassword);
            }
        }catch (Exception e){
            log.log(Level.SEVERE, "Error changing Trainee Password {0}", e.getMessage());
        }
    }

    public void updateTrainerProfile(Trainer trainer, String username, String password) {
        try {
            if (matchTrainerUsernameAndPassword(username, password)) {
                log.log(Level.INFO, "Updating Trainer Profile {0}", username);
                trainerService.updateTrainerProfile(trainer);
            }
        }catch (Exception e){
            log.log(Level.SEVERE, "Error updating Trainer Profile {0}", e.getMessage());
        }
    }

    public void updateTraineeProfile(Trainee trainee, String username, String password) {
        try {
            if (matchTraineeUsernameAndPassword(username, password)) {
                log.log(Level.INFO, "Updating Trainee Profile {0}", username);
                traineeService.updateTraineeProfile(trainee);
            }
        }catch (Exception e){
            log.log(Level.SEVERE, "Error updating Trainee Profile {0}", e.getMessage());
        }
    }

    public boolean activateDeactivateTrainer(String username, String password, String usernameToChange) {
        try {
            if (matchTrainerUsernameAndPassword(username, password)) {
                log.log(Level.INFO, "Activating/Deactivating Trainer {0}", usernameToChange);
                trainerService.activateDeactivateTrainer(usernameToChange);
                return true;
            }
            log.log(Level.WARNING, "Trainer username and password do not match");
            return false;
        }catch (Exception e){
            log.log(Level.SEVERE, "Error activating/deactivating Trainer {0}", e.getMessage());
            return false;
        }
    }

    public boolean activateDeactivateTrainee(String username, String password, String usernameToChange) {
        try {
            if (matchTrainerUsernameAndPassword(username, password)) {
                log.log(Level.INFO, "Activating/Deactivating Trainee {0}", usernameToChange);
                traineeService.activateDeactivateTrainee(usernameToChange);
                return true;
            }
            log.log(Level.WARNING, "Trainer username and password do not match");
            return false;
        }catch (Exception e){
            log.log(Level.SEVERE, "Error activating/deactivating Trainer {0}", e.getMessage());
            return false;
        }
    }

    public boolean deleteTraineeByUsername(String username, String password, String usernameToDelete) {
        try {
            if (matchTraineeUsernameAndPassword(username, password)) {
                log.log(Level.INFO, "Deleting Trainee {0}", usernameToDelete);
                traineeService.deleteTrainee(usernameToDelete);
                return true;
            }
            log.log(Level.WARNING, "Trainee username and password do not match");
            return false;
        }catch (Exception e){
            log.log(Level.SEVERE, "Error deleting Trainee {0}", e.getMessage());
            return false;
        }
    }

    public List<Training> getTraineeTrainingsByTraineeUsernameFromDateToDateTrainerNameTrainingType(String traineeUsername, Date fromDate, Date toDate, String trainerName, String trainingType) {
        try {
            log.log(Level.INFO, "Getting Trainee Trainings {0}", traineeUsername);
            return trainingService.getTraineeTrainings(traineeUsername, fromDate, toDate, trainerName, trainingType);
        }catch (Exception e){
            log.log(Level.SEVERE, "Error getting Trainee Trainings {0}", e.getMessage());
            return List.of();
        }

    }

    public List<Training> getTrainerTrainingsByTrainerUsernameFromDateToDateTraineeName(String trainerUsername, Date fromDate, Date toDate, String traineeName) {
        try {
            log.log(Level.INFO, "Getting Trainer Trainings {0}", trainerUsername);
            return trainingService.getTrainerTrainings(trainerUsername, fromDate, toDate, traineeName);
        }catch  (Exception e){
            log.log(Level.SEVERE, "Error getting Trainer Trainings {0}", e.getMessage());
            return List.of();
        }
    }

    public Optional<Training> createTraining(Training training, String username, String password) {
        try {
            if (matchTrainerUsernameAndPassword(username, password)) {
                log.log(Level.INFO, "Creating Training {0}", training.getTrainingName());
                return trainingService.createTraining(training);
            }
            log.log(Level.WARNING, "Trainer username and password do not match");
            return Optional.empty();
        }catch (Exception e){
            log.log(Level.SEVERE, "Error creating Training {0}", e.getMessage());
            return Optional.empty();
        }
    }

    public List<Trainer> getTrainersNotInTrainersTraineeListByTraineeUserUsername(String traineeUsername, String password) {
        try {
            if (matchTraineeUsernameAndPassword(traineeUsername, password)) {
                log.log(Level.INFO, "Getting Trainers not in Trainee's Trainer List {0}", traineeUsername);
                return traineeService.getTrainersNotInTrainersTraineeListByTraineeUserUsername(traineeUsername);
            }
            log.log(Level.WARNING, "Trainee username and password do not match");
            return List.of();
        }catch (Exception e){
            log.log(Level.SEVERE, "Error getting Trainers not in Trainee's Trainer List {0}", e.getMessage());
            return List.of();
        }
    }

    public boolean updateTrainersTraineeList(String traineeUsername, String traineePassword, String trainerUsername) {
        try {
            if (matchTraineeUsernameAndPassword(traineeUsername, traineePassword)) {
                log.log(Level.INFO, "Updating Trainers Trainee List {0}", traineeUsername);
                return traineeService.updateTrainersTraineeList(traineeUsername, trainerUsername);
            }
            log.log(Level.WARNING, "Trainee username and password do not match");
            return false;
        }catch (Exception e){
            log.log(Level.SEVERE, "Error updating Trainers Trainee List {0}", e.getMessage());
            return false;
        }
    }
}
