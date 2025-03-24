package gym.crm.backend.service;

import gym.crm.backend.domain.entities.*;
import gym.crm.backend.domain.request.TrainingCreationRequest;
import gym.crm.backend.domain.response.training.TrainingTraineesResponse;
import gym.crm.backend.domain.response.training.TrainingTrainersResponse;
import gym.crm.backend.domain.response.trainingType.TrainingTypeResponse;
import gym.crm.backend.exception.runtimeException.DateParsingFailedException;
import gym.crm.backend.exception.entityNotFoundException.ProfileNotFoundException;
import gym.crm.backend.repository.TraineeRepository;
import gym.crm.backend.repository.TrainerRepository;
import gym.crm.backend.repository.TrainingRepository;
import gym.crm.backend.repository.TrainingTypeRepository;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Slf4j
public class TrainingService {

    private final TrainingRepository trainingRepository;
    private final TrainerRepository trainerRepository;
    private final TraineeRepository traineeRepository;
    private final TrainingTypeRepository trainingTypeRepository;

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

    public Page<TrainingTrainersResponse> getTrainerTrainings(String username, String fromDate, String toDate, String traineeName, Pageable pageable){

        Date fromDateParsed = parseDate(fromDate);
        Date toDateParsed = parseDate(toDate);
        Page<Training> trainingList = trainingRepository.findTrainerTrainings(username, fromDateParsed, toDateParsed, traineeName, pageable);

        return trainingList.map(
                training -> new TrainingTrainersResponse(
                        training.getTrainingName(),
                        training.getTrainingDate(),
                        training.getTrainingType().toString(),
                        training.getTrainingDuration(),
                        training.getTrainee().getUser().getFirstName() + " " + training.getTrainee().getUser().getLastName()
                )
        );
    }

    public Page<TrainingTraineesResponse> getTraineeTrainings(String username,
                                                              String fromDate,
                                                              String toDate,
                                                              String trainerName,
                                                              String trainingType,
                                                              Pageable pageable){

        Date fromDateParsed = parseDate(fromDate);
        Date toDateParsed = parseDate(toDate);

        if (trainingType == null || trainingType.isEmpty()) {
            return trainingRepository.findTraineeTrainings(
                    username,
                    fromDateParsed,
                    toDateParsed,
                    trainerName,
                    null,
                    pageable
            ).map(
                    training -> new TrainingTraineesResponse(
                            training.getTrainingName(),
                            training.getTrainingDate(),
                            training.getTrainingType().toString(),
                            training.getTrainingDuration(),
                            training.getTrainer().getUser().getFirstName() + " " + training.getTrainer().getUser().getLastName()
                    )
            );
        }
        TrainingType trainingTypeEntity = trainingTypeRepository.findByTrainingTypeName(TrainingTypes.valueOf(trainingType)).orElse(null);

        return trainingRepository.findTraineeTrainings(
                username,
                fromDateParsed,
                toDateParsed,
                trainerName,
                trainingTypeEntity,
                pageable
        ).map(
                training -> new TrainingTraineesResponse(
                        training.getTrainingName(),
                        training.getTrainingDate(),
                        training.getTrainingType().toString(),
                        training.getTrainingDuration(),
                        training.getTrainer().getUser().getFirstName() + " " + training.getTrainer().getUser().getLastName()
                )
        );
    }

    public void createTraining(TrainingCreationRequest training) {
        String transactionId = MDC.get("transactionId");

        Training trainingEntity = new Training();
        trainingEntity.setTrainingName(training.getTrainingName());
        trainingEntity.setTrainingDate(training.getTrainingDate());
        trainingEntity.setTrainingDuration(training.getTrainingDuration());

        String trainerUsername = training.getTrainerUsername();
        Trainer trainer = trainerRepository.findByUserUsername(trainerUsername).orElseThrow(
                () -> {
                    log.error("Transaction ID: {}. Trainer with username: {} not found", transactionId, trainerUsername);
                    return new ProfileNotFoundException("Trainer with username: " + trainerUsername + " not found");
                });

        trainingEntity.setTrainer(trainer);

        String traineeUsername = training.getTraineeUsername();
        Trainee trainee = traineeRepository.findByUserUsername(traineeUsername).orElseThrow(
                () -> {
                    log.error("Transaction ID: {}. Trainee with username: {} not found", transactionId, traineeUsername);
                    return new ProfileNotFoundException("Trainee with username: " + traineeUsername + " not found");
                });
        trainingEntity.setTrainee(trainee);
        trainingEntity.setTrainingType(trainer.getSpecialization());

        trainingRepository.save(trainingEntity);
    }

    public Page<TrainingTypeResponse> getTrainingTypes(Pageable pageable) {
        return trainingTypeRepository.findAll(pageable).map(
                trainingType -> new TrainingTypeResponse(
                        trainingType.getTrainingTypeName().toString(),
                        trainingType.getId()
                )
        );
    }

    private Date parseDate(String dateStr) {
        if (dateStr == null) return null;
        try {
            return new SimpleDateFormat("yyyy-MM-dd").parse(dateStr);
        } catch (ParseException e) {
            String transactionId = MDC.get("transactionId");
            log.error("Transaction ID: {}. Invalid date format. Use yyyy-MM-dd", transactionId);
            throw new DateParsingFailedException("Invalid date format. Use yyyy-MM-dd.");
        }
    }


}

