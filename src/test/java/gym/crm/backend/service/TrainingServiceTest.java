package gym.crm.backend.service;

import gym.crm.backend.domain.Training;
import gym.crm.backend.repository.TrainingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TrainingServiceTest {

    @Mock
    private TrainingRepository trainingRepository;

    @InjectMocks
    private TrainingService trainingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getTrainerTrainings_Success() {
        List<Training> trainings = List.of(new Training());
        when(trainingRepository.findTrainerTrainings(anyString(), any(Date.class), any(Date.class), anyString())).thenReturn(trainings);

        List<Training> result = trainingService.getTrainerTrainings("trainer1", new Date(), new Date(), "trainee1");

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getTrainerTrainings_NoResults() {
        when(trainingRepository.findTrainerTrainings(anyString(), any(Date.class), any(Date.class), anyString())).thenReturn(List.of());

        List<Training> result = trainingService.getTrainerTrainings("trainer1", new Date(), new Date(), "trainee1");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getTraineeTrainings_Success() {
        List<Training> trainings = List.of(new Training());
        when(trainingRepository.findTraineeTrainings(anyString(), any(Date.class), any(Date.class), anyString(), anyString())).thenReturn(trainings);

        List<Training> result = trainingService.getTraineeTrainings("trainee1", new Date(), new Date(), "trainer1", "type1");

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getTraineeTrainings_NoResults() {
        when(trainingRepository.findTraineeTrainings(anyString(), any(Date.class), any(Date.class), anyString(), anyString())).thenReturn(List.of());

        List<Training> result = trainingService.getTraineeTrainings("trainee1", new Date(), new Date(), "trainer1", "type1");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void createTraining_Success() {
        Training training = new Training();
        training.setTrainingName("Training1");
        training.setTrainingDate(new Date());
        training.setTrainingDuration(1.0);

        when(trainingRepository.save(any(Training.class))).thenReturn(training);

        Optional<Training> result = trainingService.createTraining(training);

        assertNotNull(result);
        assertEquals("Training1", result.get().getTrainingName());
    }

    @Test
    void createTraining_Failure_EmptyName() {
        Training training = new Training();
        training.setTrainingDate(new Date());
        training.setTrainingDuration(1.0);

        Optional<Training> result = trainingService.createTraining(training);

        assertFalse(result.isPresent());
        verify(trainingRepository, never()).save(any(Training.class));
    }

    @Test
    void createTraining_Failure_EmptyDate() {
        Training training = new Training();
        training.setTrainingName("Training1");
        training.setTrainingDuration(1.0);

        Optional<Training> result = trainingService.createTraining(training);

        assertFalse(result.isPresent());
        verify(trainingRepository, never()).save(any(Training.class));
    }

    @Test
    void createTraining_Failure_EmptyDuration() {
        Training training = new Training();
        training.setTrainingName("Training1");
        training.setTrainingDate(new Date());

        Optional<Training> result = trainingService.createTraining(training);

        assertFalse(result.isPresent());
        verify(trainingRepository, never()).save(any(Training.class));
    }
}