package gym.crm.backend.facade;

import gym.crm.backend.domain.Trainee;
import gym.crm.backend.domain.Trainer;
import gym.crm.backend.domain.Training;
import gym.crm.backend.service.TraineeService;
import gym.crm.backend.service.TrainerService;
import gym.crm.backend.service.TrainingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class Facade {
    private final TrainerService trainerService;
    private final TraineeService traineeService;
    private final TrainingService trainingService;
    private static final Logger logger =  Logger.getLogger(Facade.class.getName());

    @Autowired
    public Facade(TrainerService trainerService, TraineeService traineeService, TrainingService trainingService) {
        this.trainerService = trainerService;
        this.traineeService = traineeService;
        this.trainingService = trainingService;
    }


    // Trainer operations
    public void createTrainer(Trainer trainer) {
        logger.log(Level.INFO, "Creating a new trainer: {0} {1}.", new Object[]{trainer.getFirstName(), trainer.getLastName()});
        trainerService.saveTrainer(trainer);
    }

    public Trainer getTrainer(long id) {
        logger.log(Level.INFO, "Finding trainer with id: {0}.", id);
        return trainerService.findTrainer(id);
    }

    public Collection<Trainer> getAllTrainers() {
        logger.log(Level.INFO, "Finding all trainers.");
        return trainerService.findAllTrainers();
    }

    public void deleteTrainer(long id) {
        logger.log(Level.INFO, "Deleting trainer with id: {0}.", id);
        trainerService.deleteTrainer(id);
    }

    // Trainee operations
    public void createTrainee(Trainee trainee) {
        logger.log(Level.INFO, "Creating a new trainee: {0} {1}.", new Object[]{trainee.getFirstName(), trainee.getLastName()});
        traineeService.saveTrainee(trainee);
    }

    public Trainee getTrainee(long id) {
        logger.log(Level.INFO, "Finding trainee with id: {0}.", id);
        return traineeService.findTrainee(id);
    }

    public Collection<Trainee> getAllTrainees() {
        logger.log(Level.INFO, "Finding all trainees.");
        return traineeService.findAllTrainees();
    }

    public void deleteTrainee(long id) {
        logger.log(Level.INFO, "Deleting trainee with id: {0}.", id);
        traineeService.deleteTrainee(id);
    }

    // Training operations
    public void createTraining(Training training) {
        logger.log(Level.INFO, "Creating a new training: {0}.", training);
        trainingService.saveTraining(training);
    }

    public Training getTraining(long id) {
        logger.log(Level.INFO, "Finding training with id: {0}.", id);
        return trainingService.findTraining(id);
    }

    // Utility method
    public void printAllData() {
        System.out.println("--- Trainers ---");
        getAllTrainers().forEach(System.out::println);

        System.out.println("--- Trainees ---");
        getAllTrainees().forEach(System.out::println);
    }

    public void run(){
        logger.log(Level.INFO, "Running the application.");
        printAllData();
    }
}
