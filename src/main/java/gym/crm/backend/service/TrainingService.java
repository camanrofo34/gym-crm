package gym.crm.backend.service;

import gym.crm.backend.domain.entities.Trainee;
import gym.crm.backend.domain.entities.Trainer;
import gym.crm.backend.domain.entities.Training;
import gym.crm.backend.domain.request.TrainingCreationRequest;
import gym.crm.backend.domain.response.training.TrainingTraineesResponse;
import gym.crm.backend.domain.response.training.TrainingTrainersResponse;
import gym.crm.backend.domain.response.trainingType.TrainingTypeResponse;
import gym.crm.backend.repository.TraineeRepository;
import gym.crm.backend.repository.TrainerRepository;
import gym.crm.backend.repository.TrainingRepository;
import gym.crm.backend.repository.TrainingTypeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class TrainingService {

    private final TrainingRepository trainingRepository;
    private final TrainerRepository trainerRepository;
    private final TraineeRepository traineeRepository;
    private final TrainingTypeRepository trainingTypeRepository;

    private final Logger log = LoggerFactory.getLogger(TrainingService.class);

    @Autowired
    public TrainingService(TrainingRepository trainingRepository,
                           TrainerRepository trainerRepository,
                           TraineeRepository traineeRepository,
                           TrainingTypeRepository trainingTypeRepository) {
        this.trainingRepository = trainingRepository;
        this.trainerRepository = trainerRepository;
        this.traineeRepository = traineeRepository;
        this.trainingTypeRepository = trainingTypeRepository;
    }

    public List<TrainingTrainersResponse> getTrainerTrainings(String username, String fromDate, String toDate, String traineeName) {
        log.info("Transaction ID: {}. Getting trainer's training list by the criteria, username: {}, fromDate: {}, toDate: {}, traineeName: {}", MDC.get("transactionId"), username, fromDate, toDate, traineeName);
        try{
            Date fromDateParsed = parseDate(fromDate);
            Date toDateParsed = parseDate(toDate);
            List<Training> trainingList = trainingRepository.findTrainerTrainings(username, fromDateParsed, toDateParsed, traineeName);
            List<TrainingTrainersResponse> trainingTrainersResponseList = new ArrayList<>();
            for (Training training : trainingList) {
                trainingTrainersResponseList.add(new TrainingTrainersResponse(training.getTrainingName(), training.getTrainingDate(), training.getTrainingType().toString(), training.getTrainingDuration(), training.getTrainee().getUser().getFirstName() + " " + training.getTrainee().getUser().getLastName()));
            }
            return  trainingTrainersResponseList;
        }catch (ParseException ex) {
            log.error("Transaction ID: {}. Failed to parse date", MDC.get("transactionId"), ex);
            return List.of();
        }
    }

    public List<TrainingTraineesResponse> getTraineeTrainings(String username, String fromDate, String toDate, String trainerName, String trainingType) {
        log.info("Transaction ID: {}. Getting trainee's training list by the criteria, username: {}, fromDate: {}, toDate: {}, trainerName: {}, trainingType: {}", MDC.get("transactionId"), username, fromDate, toDate, trainerName, trainingType);
        try{
            Date fromDateParsed = parseDate(fromDate);
            Date toDateParsed = parseDate(toDate);
            List<Training> trainingList = trainingRepository.findTraineeTrainings(username, fromDateParsed, toDateParsed, trainerName, trainingType);
            List<TrainingTraineesResponse> trainingTraineesResponseList = new ArrayList<>();
            for (Training training : trainingList) {
                trainingTraineesResponseList.add(new TrainingTraineesResponse(training.getTrainingName(), training.getTrainingDate(), training.getTrainingType().toString(), training.getTrainingDuration(), training.getTrainer().getUser().getFirstName() + " " + training.getTrainer().getUser().getLastName()));
            }
            return trainingTraineesResponseList;
        } catch (ParseException e) {
            log.error("Transaction ID: {}. Failed to parse date", MDC.get("transactionId"), e);
            return List.of();
        }
    }

    public void createTraining(TrainingCreationRequest training) {
        try {
            log.info("Transaction ID: {}. Creating training", MDC.get("transactionId"));
            Training trainingEntity = new Training();
            trainingEntity.setTrainingName(training.getTrainingName());
            trainingEntity.setTrainingDate(training.getTrainingDate());
            trainingEntity.setTrainingDuration(training.getTrainingDuration());
            Trainer trainer = trainerRepository.findByUserUsername(training.getTrainerUsername()).orElseThrow(() -> new RuntimeException("Trainer not found"));
            trainingEntity.setTrainer(trainer);
            Trainee trainee = traineeRepository.findByUserUsername(training.getTraineeUsername()).orElseThrow(() -> new RuntimeException("Trainee not found"));
            trainingEntity.setTrainee(trainee);
            trainingEntity.setTrainingType(trainer.getSpecialization());
            trainingRepository.save(trainingEntity);
        }catch (Exception e) {
            log.error("Transaction ID: {}. Failed to create training", MDC.get("transactionId"), e);
        }
    }

    public List<TrainingTypeResponse> getTrainingTypes() {
        log.info("Transaction ID: {}. Getting training types", MDC.get("transactionId"));
        List<TrainingTypeResponse> trainingTypeResponseList = new ArrayList<>();
        trainingTypeRepository.findAll().forEach(trainingTypeEntity -> {
            trainingTypeResponseList.add(new TrainingTypeResponse(trainingTypeEntity.getTrainingTypeName().toString(), trainingTypeEntity.getId()));
        });
        return trainingTypeResponseList;
    }

    private Date parseDate(String dateStr) throws ParseException {
        if (dateStr == null) return null;
        try {
            return new SimpleDateFormat("yyyy-MM-dd").parse(dateStr);
        } catch (ParseException e) {
            throw new ParseException("Invalid date format. Use yyyy-MM-dd", 0);
        }
    }
}
