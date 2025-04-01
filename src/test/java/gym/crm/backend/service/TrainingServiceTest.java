package gym.crm.backend.service;

import gym.crm.backend.domain.entities.*;
import gym.crm.backend.domain.request.TrainingCreationRequest;
import gym.crm.backend.domain.response.training.TrainingTrainersResponse;
import gym.crm.backend.repository.TraineeRepository;
import gym.crm.backend.repository.TrainerRepository;
import gym.crm.backend.repository.TrainingRepository;
import gym.crm.backend.repository.TrainingTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.text.ParseException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TrainingServiceTest {

    @Mock
    private TrainingRepository trainingRepository;

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private TraineeRepository traineeRepository;

    @Mock
    private TrainingTypeRepository trainingTypeRepository;

    @InjectMocks
    private TrainingService trainingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getTrainerTrainings_Success() throws ParseException {
        Training training = new Training();
        training.setTrainingName("Training1");
        training.setTrainingDate(new Date());
        training.setTrainingDuration(1.0);
        Trainer trainer = new Trainer();
        trainer.setUser(new User());
        training.setTrainer(trainer);
        Trainee trainee = new Trainee();
        trainee.setUser(new User());
        training.setTrainee(trainee);
        training.setTrainingType(new TrainingType());
        Page<Training> trainings = new PageImpl<>(Collections.singletonList(training));
        when(trainingRepository.findTrainerTrainings(anyString(), any(Date.class), any(Date.class), anyString(), any(Pageable.class))).thenReturn(trainings);

        Page<TrainingTrainersResponse> result = trainingService.getTrainerTrainings("trainer1", "2023-01-01", "2023-12-31", "trainee1", Pageable.unpaged());

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Training1", result.stream().toList().getFirst().getTrainingName());
    }


    @Test
    void createTraining_Success() {
        TrainingCreationRequest request = new TrainingCreationRequest();
        request.setTrainingName("Training1");
        request.setTrainingDate(new Date());
        request.setTrainingDuration(1.0);
        request.setTrainerUsername("trainer1");
        request.setTraineeUsername("trainee1");
        Trainer trainer = new Trainer();
        trainer.setSpecialization(new TrainingType());
        when(trainerRepository.findByUserUsername(anyString())).thenReturn(Optional.of(trainer));
        Trainee trainee = new Trainee();
        when(traineeRepository.findByUserUsername(anyString())).thenReturn(Optional.of(trainee));

        trainingService.createTraining(request);

        verify(trainingRepository, times(1)).save(any(Training.class));
    }
}