package gym.crm.backend.service;

import gym.crm.backend.dao.TraineeDAO;
import gym.crm.backend.domain.Trainee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TraineeServiceTest {
    @Mock
    private TraineeDAO traineeDAO;

    @InjectMocks
    private TraineeService traineeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void saveTraineeCreatesNewTrainee() {
        Trainee trainee = new Trainee("Alice", "Johnson", "01/01/1990", "Street 2");

        traineeService.saveTrainee(trainee);

        ArgumentCaptor<Trainee> captor = ArgumentCaptor.forClass(Trainee.class);
        verify(traineeDAO).saveTrainee(captor.capture());

        Trainee savedTrainee = captor.getValue();
        assertNotNull(savedTrainee.getUsername());
        assertNotNull(savedTrainee.getPassword());
    }

    @Test
    void findTraineeReturnsTrainee() {
        Trainee trainee = new Trainee("Bob", "Brown", "02/02/1992", "Street 3");
        trainee.setTraineeId(3L);

        when(traineeDAO.findTrainee(3L)).thenReturn(trainee);

        Trainee foundTrainee = traineeService.findTrainee(3);

        assertEquals(trainee, foundTrainee);
    }

    @Test
    void deleteTraineeRemovesTrainee() {
        Trainee trainee = new Trainee("Charlie", "Davis", "03/03/1993", "Street 4");
        trainee.setTraineeId(4L);

        when(traineeDAO.findTrainee(4L)).thenReturn(trainee);

        traineeService.deleteTrainee(4L);

        verify(traineeDAO).deleteTrainee(4L);
    }

    @Test
    void updateTraineeUpdatesExistingTrainee() {
        Trainee trainee = new Trainee("David", "Evans", "04/04/1994", "Street 5");
        trainee.setTraineeId(5L);

        when(traineeDAO.findTrainee(5L)).thenReturn(trainee);

        trainee.setLastName("Smith");

        traineeService.updateTrainee(trainee);

        verify(traineeDAO).updateTrainee(any(Trainee.class));
        assertEquals("Smith", trainee.getLastName());
    }

    @Test
    void findAllTraineesReturnsAllTrainees() {
        Trainee trainee1 = new Trainee("Eve", "Foster", "05/05/1995", "Street 6");
        Trainee trainee2 = new Trainee("Frank", "Green", "06/06/1996", "Street 7");

        when(traineeDAO.findAllTrainees()).thenReturn(List.of(trainee1, trainee2));

        Collection<Trainee> trainees = traineeService.findAllTrainees();

        assertEquals(2, trainees.size());
        assertTrue(trainees.contains(trainee1));
        assertTrue(trainees.contains(trainee2));
    }


}