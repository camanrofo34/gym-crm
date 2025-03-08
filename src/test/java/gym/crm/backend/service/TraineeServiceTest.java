package gym.crm.backend.service;

import gym.crm.backend.domain.Trainee;
import gym.crm.backend.domain.Trainer;
import gym.crm.backend.domain.User;
import gym.crm.backend.repository.TraineeRepository;
import gym.crm.backend.repository.TrainerRepository;
import gym.crm.backend.util.UserUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TraineeServiceTest {

    @Mock
    private TraineeRepository traineeRepository;

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private UserUtil userUtil;

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

        when(userUtil.generateUsername(anyString(), anyString(), anyList()))
                .thenReturn("john.doe");

        when(userUtil.generatePassword()).thenReturn("password123");

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
        user.setUsername("john.doe");
        trainee.setUser(user);

        when(traineeRepository.findByUserUsername("john.doe")).thenReturn(Optional.of(trainee));

        Optional<Trainee> result = traineeService.getTraineeByUsername("john.doe");

        assertTrue(result.isPresent());
        assertEquals("john.doe", result.get().getUser().getUsername());
    }

    @Test
    void getTraineeByUsername_NotFound() {
        when(traineeRepository.findByUserUsername("john.doe")).thenReturn(Optional.empty());

        Optional<Trainee> result = traineeService.getTraineeByUsername("john.doe");

        assertFalse(result.isPresent());
    }

    @Test
    void matchTraineeUsernameAndPassword_Success() {
        Trainee trainee = new Trainee();
        User user = new User();
        user.setUsername("john.doe");
        user.setPassword("password123");
        trainee.setUser(user);

        when(traineeRepository.findByUserUsername("john.doe")).thenReturn(Optional.of(trainee));

        boolean result = traineeService.matchTraineeUsernameAndPassword("john.doe", "password123");

        assertTrue(result);
    }

    @Test
    void matchTraineeUsernameAndPassword_FailureByUsername() {
        Trainee trainee = new Trainee();
        User user = new User();
        user.setUsername("john.doe");
        user.setPassword("password123");
        trainee.setUser(user);

        when(traineeRepository.findByUserUsername("john.doe")).thenReturn(Optional.of(trainee));

        boolean result = traineeService.matchTraineeUsernameAndPassword("john.doe", "wrong");

        assertFalse(result);
    }

    @Test
    void matchTraineeUsernameAndPassword_FailureByPassword() {
        when(traineeRepository.findByUserUsername("john.doe")).thenReturn(Optional.empty());

        boolean result = traineeService.matchTraineeUsernameAndPassword("john.doe", "password123");

        assertFalse(result);
    }

    @Test
    void changePassword_Success() {
        Trainee trainee = new Trainee();
        User user = new User();
        user.setUsername("john.doe");
        trainee.setUser(user);

        when(traineeRepository.findByUserUsername("john.doe")).thenReturn(Optional.of(trainee));

        traineeService.changePassword("john.doe", "newpassword");

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
        user.setUsername("john.doe");
        trainee.setUser(user);

        when(traineeRepository.findByUserUsername("john.doe")).thenReturn(Optional.of(trainee));

        traineeService.deleteTrainee("john.doe");

        verify(traineeRepository, times(1)).delete(trainee);
    }

    @Test
    void deleteTrainee_TraineeNotFound() {
        when(traineeRepository.findByUserUsername("johndoe")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> traineeService.deleteTrainee("johndoe"));
    }

    @Test
    void updateTrainee_Success() {
        Trainee trainee = new Trainee();
        User user = new User();
        user.setFirstName("john");
        user.setLastName("doe");
        user.setUsername("john.doe");
        trainee.setUser(user);

        when(traineeRepository.findByUserUsername("john.doe")).thenReturn(Optional.of(trainee));

        traineeService.updateTraineeProfile(trainee);

        verify(traineeRepository, times(1)).save(trainee);
    }

    @Test
    void updateTrainee_Failure_InvalidData() {
        Trainee trainee = new Trainee();

        traineeService.updateTraineeProfile(trainee);

        verify(traineeRepository, never()).save(trainee);
    }

    @Test
    void activateDeactivateTrainee_Success() {
        Trainee trainee = new Trainee();
        User user = new User();
        user.setUsername("john.doe");
        user.setIsActive(true);
        trainee.setUser(user);

        when(traineeRepository.findByUserUsername("john.doe")).thenReturn(Optional.of(trainee));

        traineeService.activateDeactivateTrainee("john.doe");

        assertFalse(trainee.getUser().getIsActive());
        verify(traineeRepository, times(1)).save(trainee);
    }

    @Test
    void activateDeactivateTrainee_TraineeNotFound() {
        when(traineeRepository.findByUserUsername("john.doe")).thenReturn(Optional.empty());

        traineeService.activateDeactivateTrainee("john.doe");

        verify(traineeRepository, never()).save(any(Trainee.class));
    }

    @Test
    void getTrainersNotInTrainersTraineeListByTraineeUserUsername_Success() {
        // Arrange
        String traineeUsername = "john.doe";

        Trainer trainer1 = new Trainer();
        User user1 = new User();
        trainer1.setUser(user1);
        trainer1.getUser().setFirstName("Trainer One");

        Trainer trainer2 = new Trainer();
        User user2 = new User();
        trainer2.setUser(user2);
        trainer2.getUser().setFirstName("Trainer Two");

        List<Trainer> expectedTrainers = List.of(trainer1, trainer2);

        // Simulating repository behavior
        when(traineeRepository.findTrainersNotInTrainersTraineeListByTraineeUserUsername(traineeUsername))
                .thenReturn(expectedTrainers);

        // Act
        List<Trainer> result = traineeService.getTrainersNotInTrainersTraineeListByTraineeUserUsername(traineeUsername);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Trainer One", result.get(0).getUser().getFirstName());
        assertEquals("Trainer Two", result.get(1).getUser().getFirstName());

        verify(traineeRepository, times(1))
                .findTrainersNotInTrainersTraineeListByTraineeUserUsername(traineeUsername);
    }

    @Test
    void getTrainersNotInTrainersTraineeListByTraineeUserUsername_EmptyList() {
        // Arrange
        String traineeUsername = "unknown.user";
        when(traineeRepository.findTrainersNotInTrainersTraineeListByTraineeUserUsername(traineeUsername))
                .thenReturn(Collections.emptyList());

        // Act
        List<Trainer> result = traineeService.getTrainersNotInTrainersTraineeListByTraineeUserUsername(traineeUsername);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(traineeRepository, times(1))
                .findTrainersNotInTrainersTraineeListByTraineeUserUsername(traineeUsername);
    }

    @Test
    void updateTrainersTraineeList_Success_NewTrainer() {
        // Arrange
        String username = "john.doe";

        Trainee trainee = new Trainee();
        trainee.setUser(new User());
        trainee.setTrainers(new HashSet<>());

        Trainer trainer = new Trainer();
        String usernameTrainer = "trainer.user";
        trainer.setUser(new User());

        when(traineeRepository.findByUserUsername(username)).thenReturn(Optional.of(trainee));
        when(trainerRepository.findByUserUsername(usernameTrainer)).thenReturn(Optional.of(trainer));

        // Act
        boolean result = traineeService.updateTrainersTraineeList(username, usernameTrainer);

        // Assert
        assertTrue(result);
        assertEquals(1, trainee.getTrainers().size());
        assertTrue(trainee.getTrainers().contains(trainer));

        verify(traineeRepository, times(1)).save(trainee);
    }

    @Test
    void updateTrainersTraineeList_TrainerAlreadyExists() {
        // Arrange
        String username = "john.doe";

        Trainer trainer = new Trainer();
        String usernameTrainer = "trainer.user";

        Set<Trainer> trainers = new HashSet<>();
        trainers.add(trainer);

        Trainee trainee = new Trainee();
        trainee.setUser(new User());
        trainee.setTrainers(trainers);

        when(traineeRepository.findByUserUsername(username)).thenReturn(Optional.of(trainee));
        when(trainerRepository.findByUserUsername(usernameTrainer)).thenReturn(Optional.of(trainer));

        // Act
        boolean result = traineeService.updateTrainersTraineeList(username, usernameTrainer);

        // Assert
        assertFalse(result);
        assertEquals(1, trainee.getTrainers().size());

        verify(traineeRepository, never()).save(trainee);
    }

    @Test
    void updateTrainersTraineeList_TraineeNotFound() {
        // Arrange
        String username = "nonexistent.user";

        when(traineeRepository.findByUserUsername(username)).thenReturn(Optional.empty());

        // Act
        boolean result = traineeService.updateTrainersTraineeList(username, "trainer.user");

        // Assert
        assertFalse(result);
        verify(traineeRepository, never()).save(any(Trainee.class));
    }

    @Test
    void updateTrainersTraineeList_TrainerNotFound() {
        // Arrange
        String username = "john.doe";

        Trainee trainee = new Trainee();
        trainee.setUser(new User());
        trainee.setTrainers(new HashSet<>());
        String usernameTrainer = "trainer.user";

        when(traineeRepository.findByUserUsername(username)).thenReturn(Optional.of(trainee));
        when(trainerRepository.findByUserUsername(usernameTrainer)).thenReturn(Optional.empty());

        // Act
        boolean result = traineeService.updateTrainersTraineeList(username, usernameTrainer);

        // Assert
        assertFalse(result);
        verify(traineeRepository, never()).save(any(Trainee.class));
    }
}