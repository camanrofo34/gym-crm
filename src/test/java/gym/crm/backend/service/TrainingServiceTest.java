package gym.crm.backend.service;

import gym.crm.backend.domain.entities.*;
import gym.crm.backend.domain.request.TrainingCreationRequest;
import gym.crm.backend.domain.response.training.TrainingTraineesResponse;
import gym.crm.backend.domain.response.training.TrainingTrainersResponse;
import gym.crm.backend.domain.response.trainingType.TrainingTypeResponse;
import gym.crm.backend.repository.TraineeRepository;
import gym.crm.backend.repository.TrainerRepository;
import gym.crm.backend.repository.TrainingRepository;
import gym.crm.backend.repository.TrainingTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

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
        List<Training> trainings = new ArrayList<>();
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
        trainings.add(training);
        when(trainingRepository.findTrainerTrainings(anyString(), any(Date.class), any(Date.class), anyString())).thenReturn(trainings);

        List<TrainingTrainersResponse> result = trainingService.getTrainerTrainings("trainer1", "2023-01-01", "2023-12-31", "trainee1");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Training1", result.getFirst().getTrainingName());
    }

    @Test
    void getTrainerTrainings_InvalidDates() {
        List<TrainingTrainersResponse> result = trainingService.getTrainerTrainings("trainer1", "invalid-date", "invalid-date", "trainee1");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getTraineeTrainings_Success() throws ParseException {
        List<Training> trainings = new ArrayList<>();
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
        trainings.add(training);
        when(trainingRepository.findTraineeTrainings(anyString(), any(Date.class), any(Date.class), anyString(), anyString())).thenReturn(trainings);

        List<TrainingTraineesResponse> result = trainingService.getTraineeTrainings("trainee1", "2023-01-01", "2023-12-31", "trainer1", "type1");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Training1", result.getFirst().getTrainingName());
    }

    @Test
    void getTraineeTrainings_InvalidDates() {
        List<TrainingTraineesResponse> result = trainingService.getTraineeTrainings("trainee1", "invalid-date", "invalid-date", "trainer1", "type1");

        assertNotNull(result);
        assertTrue(result.isEmpty());
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

    @Test
    void getTrainingTypes_Success() {
        List<TrainingType> trainingTypes = new ArrayList<>();
        TrainingType trainingType = new TrainingType(1L, TrainingTypes.RESISTANCE, new ArrayList<>(), new ArrayList<>());
        trainingTypes.add(trainingType);
        when(trainingTypeRepository.findAll()).thenReturn(trainingTypes);

        List<TrainingTypeResponse> result = trainingService.getTrainingTypes();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("RESISTANCE", result.getFirst().getTrainingTypeName());
    }
}