package gym.crm.backend.service;

import gym.crm.backend.domain.Trainee;
import gym.crm.backend.domain.User;
import gym.crm.backend.repository.TraineeRepository;
import gym.crm.backend.repository.TrainerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TraineeServiceTest {

    @Mock
    private TraineeRepository traineeRepository;

    @Mock
    private TrainerRepository trainerRepository;

    @InjectMocks
    private TraineeService traineeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createTrainee_Success() {
        Trainee trainee = new Trainee();
        User user = new User();
        user.setFirstName("John");
        user.setLastName("Doe");
        trainee.setUser(user);

        when(traineeRepository.save(any(Trainee.class))).thenReturn(trainee);

        Trainee result = traineeService.createTrainee(trainee);

        assertNotNull(result);
        assertEquals("John", result.getUser().getFirstName());
        verify(traineeRepository, times(1)).save(trainee);
    }

    @Test
    void createTrainee_Failure_InvalidData() {
        Trainee trainee = new Trainee();

        Trainee result = traineeService.createTrainee(trainee);

        assertNull(result);
        verify(traineeRepository, never()).save(any(Trainee.class));
    }

    @Test
    void getTraineeByUsername_Success() {
        Trainee trainee = new Trainee();
        User user = new User();
        user.setUsername("johndoe");
        trainee.setUser(user);

        when(traineeRepository.findByUserUsername("johndoe")).thenReturn(Optional.of(trainee));

        Optional<Trainee> result = traineeService.getTraineeByUsername("johndoe");

        assertTrue(result.isPresent());
        assertEquals("johndoe", result.get().getUser().getUsername());
    }

    @Test
    void getTraineeByUsername_NotFound() {
        when(traineeRepository.findByUserUsername("johndoe")).thenReturn(Optional.empty());

        Optional<Trainee> result = traineeService.getTraineeByUsername("johndoe");

        assertFalse(result.isPresent());
    }

    @Test
    void matchTraineeUsernameAndPassword_Success() {
        Trainee trainee = new Trainee();
        User user = new User();
        user.setUsername("johndoe");
        user.setPassword("password123");
        trainee.setUser(user);

        when(traineeRepository.findByUserUsername("johndoe")).thenReturn(Optional.of(trainee));

        boolean result = traineeService.matchTraineeUsernameAndPassword("johndoe", "password123");

        assertTrue(result);
    }

    @Test
    void matchTraineeUsernameAndPassword_Failure() {
        when(traineeRepository.findByUserUsername("johndoe")).thenReturn(Optional.empty());

        boolean result = traineeService.matchTraineeUsernameAndPassword("johndoe", "password123");

        assertFalse(result);
    }

    @Test
    void changePassword_Success() {
        Trainee trainee = new Trainee();
        User user = new User();
        user.setUsername("johndoe");
        trainee.setUser(user);

        when(traineeRepository.findByUserUsername("johndoe")).thenReturn(Optional.of(trainee));

        traineeService.changePassword("johndoe", "newpassword");

        assertEquals("newpassword", trainee.getUser().getPassword());
        verify(traineeRepository, times(1)).save(trainee);
    }

    @Test
    void changePassword_TraineeNotFound() {
        when(traineeRepository.findByUserUsername("johndoe")).thenReturn(Optional.empty());

        traineeService.changePassword("johndoe", "newpassword");

        verify(traineeRepository, never()).save(any(Trainee.class));
    }

    @Test
    void deleteTrainee_Success() {
        Trainee trainee = new Trainee();
        User user = new User();
        user.setUsername("johndoe");
        trainee.setUser(user);

        when(traineeRepository.findByUserUsername("johndoe")).thenReturn(Optional.of(trainee));

        traineeService.deleteTrainee("johndoe");

        verify(traineeRepository, times(1)).delete(trainee);
    }

    @Test
    void deleteTrainee_TraineeNotFound() {
        when(traineeRepository.findByUserUsername("johndoe")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> traineeService.deleteTrainee("johndoe"));
    }
}