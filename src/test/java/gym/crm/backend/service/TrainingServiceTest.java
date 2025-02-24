package gym.crm.backend.service;

import gym.crm.backend.dao.TrainingDAO;
import gym.crm.backend.domain.Training;
import gym.crm.backend.domain.TrainingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TrainingServiceTest {

    @Mock
    private TrainingDAO trainingDAO;

    @InjectMocks
    private TrainingService trainingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void saveTrainingCreatesNewTraining() {
        Training training = new Training(1,1,1, "Strength Training",
                new TrainingType(1, "Strengh"), "20/10/2000",
                30.0);

        trainingService.saveTraining(training);

        ArgumentCaptor<Training> captor = ArgumentCaptor.forClass(Training.class);
        verify(trainingDAO).saveTraining(captor.capture());

        Training savedTraining = captor.getValue();
        assertNotNull(savedTraining);
        assertEquals("Strength Training", savedTraining.getTrainingName());
    }

    @Test
    void findTrainingReturnsTraining() {
        Training training = new Training(1,1,1, "Strength Training",
                new TrainingType(1, "Strengh"), "20/10/2000",
                30.0);

        when(trainingDAO.findTraining(1)).thenReturn(training);

        Training foundTraining = trainingService.findTraining(1);

        assertEquals(training, foundTraining);
    }

    @Test
    void findTrainingReturnsNullForNonExistentId() {
        when(trainingDAO.findTraining(999)).thenReturn(null);

        Training foundTraining = trainingService.findTraining(999);

        assertNull(foundTraining);
    }
}