package gym.crm.backend.service;

import gym.crm.backend.domain.entities.*;
import gym.crm.backend.domain.request.TraineeCreationRequest;
import gym.crm.backend.domain.request.TraineeUpdateRequest;
import gym.crm.backend.domain.response.trainee.TraineeGetProfileResponse;
import gym.crm.backend.domain.response.trainee.TraineeUpdateResponse;
import gym.crm.backend.domain.response.UserCreationResponse;
import gym.crm.backend.domain.response.trainee.TrainersTraineeResponse;
import gym.crm.backend.repository.TraineeRepository;
import gym.crm.backend.repository.TrainerRepository;
import gym.crm.backend.util.UserUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
        TraineeCreationRequest request = new TraineeCreationRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setAddress("123 Main St");
        List<Trainee> trainees = new ArrayList<>();
        when(traineeRepository.findAll()).thenReturn(trainees);
        when(userUtil.generateUsername(anyString(), anyString(), anyList())).thenReturn("johndoe");
        when(userUtil.generatePassword()).thenReturn("password123");
        when(traineeRepository.save(any(Trainee.class))).thenReturn(new Trainee());

        Optional<UserCreationResponse> response = traineeService.createTrainee(request);

        assertTrue(response.isPresent());
        assertEquals("johndoe", response.get().getUsername());
        assertEquals("password123", response.get().getPassword());
    }

    @Test
    void createTrainee_Failure_GenerateUsername() {
        TraineeCreationRequest request = new TraineeCreationRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        List<Trainee> trainees = new ArrayList<>();
        when(traineeRepository.findAll()).thenReturn(trainees);
        when(userUtil.generateUsername(anyString(), anyString(), anyList())).thenReturn("");

        assertThrows(RuntimeException.class, () -> traineeService.createTrainee(request));
    }

    @Test
    void createTrainee_Failure_GeneratePassword() {
        TraineeCreationRequest request = new TraineeCreationRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        List<Trainee> trainees = new ArrayList<>();
        when(traineeRepository.findAll()).thenReturn(trainees);
        when(userUtil.generateUsername(anyString(), anyString(), anyList())).thenReturn("johndoe");
        when(userUtil.generatePassword()).thenReturn(null);

        assertThrows(RuntimeException.class, () -> traineeService.createTrainee(request));
    }

    @Test
    void getTraineeByUsername_Success() {
        Trainee trainee = new Trainee();
        User user = new User();
        user.setUsername("johndoe");
        trainee.setUser(user);
        trainee.setTrainers(List.of());
        when(traineeRepository.findByUserUsername(anyString())).thenReturn(Optional.of(trainee));

        Optional<TraineeGetProfileResponse> response = traineeService.getTraineeByUsername("johndoe");

        assertTrue(response.isPresent());
    }

    @Test
    void getTraineeByUsername_NotFound() {
        when(traineeRepository.findByUserUsername(anyString())).thenReturn(Optional.empty());

        Optional<TraineeGetProfileResponse> response = traineeService.getTraineeByUsername("johndoe");

        assertFalse(response.isPresent());
    }

    @Test
    void updateTrainee_Success() {
        Trainee trainee = new Trainee();
        User user = new User();
        user.setUsername("johndoe");
        trainee.setUser(user);
        trainee.setTrainers(List.of());
        when(traineeRepository.findByUserUsername(anyString())).thenReturn(Optional.of(trainee));
        when(traineeRepository.save(any(Trainee.class))).thenReturn(trainee);

        TraineeUpdateRequest request = new TraineeUpdateRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setAddress("123 Main St");

        Optional<TraineeUpdateResponse> response = traineeService.updateTrainee("johndoe", request);

        assertTrue(response.isPresent());
        assertEquals("John", response.get().getFirstName());
        assertEquals("Doe", response.get().getLastName());
    }

    @Test
    void updateTrainee_NotFound() {
        when(traineeRepository.findByUserUsername(anyString())).thenReturn(Optional.empty());

        TraineeUpdateRequest request = new TraineeUpdateRequest();
        request.setFirstName("John");
        request.setLastName("Doe");

        Optional<TraineeUpdateResponse> response = traineeService.updateTrainee("johndoe", request);

        assertFalse(response.isPresent());
    }

    @Test
    void deleteTrainee_Success() {
        Trainee trainee = new Trainee();
        User user = new User();
        user.setUsername("johndoe");
        trainee.setUser(user);
        when(traineeRepository.findByUserUsername(anyString())).thenReturn(Optional.of(trainee));

        traineeService.deleteTrainee("johndoe");

        verify(traineeRepository, times(1)).delete(trainee);
    }

    @Test
    void deleteTrainee_NotFound() {
        when(traineeRepository.findByUserUsername(anyString())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> traineeService.deleteTrainee("johndoe"));
    }

    @Test
    void getTrainersNotInTrainersTraineeListByTraineeUserUsername_Success() {
        List<Trainer> trainers = new ArrayList<>();
        Trainer trainer = new Trainer();
        User user = new User();
        user.setUsername("trainer1");
        trainer.setUser(user);
        trainer.setSpecialization(new TrainingType(1L, TrainingTypes.RESISTANCE, List.of(), List.of()));
        trainers.add(trainer);
        when(traineeRepository.findTrainersNotInTrainersTraineeListByTraineeUserUsername(anyString())).thenReturn(trainers);

        List<TrainersTraineeResponse> response = traineeService.getTrainersNotInTrainersTraineeListByTraineeUserUsername("johndoe");

        assertEquals(1, response.size());
        assertEquals("trainer1", response.getFirst().getTrainerUsername());
    }

    @Test
    void updateTrainersTraineeList_Success() {
        Trainee trainee = new Trainee();
        User user = new User();
        user.setUsername("johndoe");
        trainee.setUser(user);
        when(traineeRepository.findByUserUsername(anyString())).thenReturn(Optional.of(trainee));
        List<Trainer> trainers = new ArrayList<>();
        Trainer trainer = new Trainer();
        trainer.setSpecialization(new TrainingType(1L, TrainingTypes.RESISTANCE, List.of(), List.of()));
        trainer.setTrainees(new ArrayList<>());
        User trainerUser = new User();
        trainerUser.setUsername("trainer1");
        trainer.setUser(trainerUser);
        trainers.add(trainer);
        when(trainerRepository.findAll()).thenReturn(trainers);

        List<String> trainerUsernames = List.of("trainer1");
        List<TrainersTraineeResponse> response = traineeService.updateTrainersTraineeList("johndoe", trainerUsernames);

        assertEquals(1, response.size());
        assertEquals("trainer1", response.getFirst().getTrainerUsername());
        verify(traineeRepository, times(1)).save(trainee);
    }
}