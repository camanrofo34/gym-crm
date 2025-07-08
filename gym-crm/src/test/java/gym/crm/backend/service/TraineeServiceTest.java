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
import gym.crm.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import gym.crm.backend.exception.types.notFound.ProfileNotFoundException;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TraineeServiceTest {

    @Mock
    private TraineeRepository traineeRepository;

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserCredentialService userCredentialService;


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

        when(userCredentialService.generateUsername(anyString(), anyString())).thenReturn("johndoe");
        when(userCredentialService.generatePassword()).thenReturn("password123");
        when(traineeRepository.save(any(Trainee.class))).thenReturn(new Trainee());

        UserCreationResponse response = traineeService.createTrainee(request);

        assertEquals("johndoe", response.getUsername());
        assertEquals("password123", response.getPassword());
    }

    @Test
    void getTraineeByUsername_Success() {
        String username = "johndoe";
        Trainee trainee = new Trainee();
        trainee.setTrainers(new HashSet<>());
        User user = new User();
        user.setFirstName("john");
        user.setUsername(username);
        trainee.setUser(user);

        when(traineeRepository.findByUserUsername(username)).thenReturn(Optional.of(trainee));

        TraineeGetProfileResponse response = traineeService.getTraineeByUsername(username);

        assertNotNull(response);
        assertEquals("john", response.getFirstName());
    }

    @Test
    void getTraineeByUsername_NotFound() {
        String username = "unknown";

        when(traineeRepository.findByUserUsername(username)).thenReturn(Optional.empty());

        assertThrows(ProfileNotFoundException.class, () -> traineeService.getTraineeByUsername(username));
    }

    @Test
    void updateTrainee_Success() {
        String username = "johndoe";
        TraineeUpdateRequest request = new TraineeUpdateRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setAddress("123 Main St");

        Trainee trainee = new Trainee();
        trainee.setTrainers(new HashSet<>());
        User user = new User();
        user.setUsername(username);
        trainee.setUser(user);

        when(traineeRepository.findByUserUsername(username)).thenReturn(Optional.of(trainee));
        when(traineeRepository.save(any(Trainee.class))).thenReturn(trainee);

        TraineeUpdateResponse response = traineeService.updateTrainee(username, request);

        assertNotNull(response);
        assertEquals("John", response.getFirstName());
        assertEquals("Doe", response.getLastName());
    }

    @Test
    void updateTrainee_NotFound() {
        String username = "unknown";
        TraineeUpdateRequest request = new TraineeUpdateRequest();

        when(traineeRepository.findByUserUsername(username)).thenReturn(Optional.empty());

        assertThrows(ProfileNotFoundException.class, () -> traineeService.updateTrainee(username, request));
    }

    @Test
    void deleteTrainee_Success() {
        String username = "johndoe";
        Trainee trainee = new Trainee();
        trainee.setTrainers(new HashSet<>());
        User user = new User();
        user.setUsername(username);
        trainee.setUser(user);

        when(traineeRepository.findByUserUsername(username)).thenReturn(Optional.of(trainee));

        traineeService.deleteTrainee(username);

        verify(traineeRepository, times(1)).delete(trainee);
    }

    @Test
    void deleteTrainee_NotFound() {
        String username = "unknown";

        when(traineeRepository.findByUserUsername(username)).thenReturn(Optional.empty());

        assertThrows(ProfileNotFoundException.class, () -> traineeService.deleteTrainee(username));
    }

    @Test
    void getTrainersNotInTrainersTraineeListByTraineeUserUsername_Success() {
        String traineeUsername = "johndoe";
        Trainee trainee = new Trainee();
        User user = new User();
        user.setUsername(traineeUsername);
        trainee.setUser(user);

        Trainer trainer = new Trainer();
        trainer.setTrainees(new HashSet<>());
        trainer.setSpecialization(new TrainingType());
        User trainerUser = new User();
        trainerUser.setUsername("trainer1");
        trainer.setUser(trainerUser);
        Page<Trainer> trainers = new PageImpl<>(Collections.singletonList(trainer));

        when(traineeRepository.findByUserUsername(traineeUsername)).thenReturn(Optional.of(trainee));
        when(traineeRepository.findTrainersNotInTrainersTraineeListByTraineeUserUsername(anyString(), any(Pageable.class))).thenReturn(trainers);

        Page<TrainersTraineeResponse> response = traineeService.getTrainersNotInTrainersTraineeListByTraineeUserUsername(traineeUsername, Pageable.unpaged());

        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
    }

    @Test
    void updateTrainersTraineeList_Success() {
        String username = "johndoe";
        List<String> trainerUsernames = Arrays.asList("trainer1", "trainer2");

        Trainee trainee = new Trainee();
        trainee.setTrainers(new HashSet<>());
        User user = new User();
        user.setUsername(username);
        trainee.setUser(user);

        Trainer trainer1 = new Trainer();
        trainer1.setTrainees(new HashSet<>());
        trainer1.setSpecialization(new TrainingType());
        User trainerUser1 = new User();
        trainerUser1.setUsername("trainer1");
        trainer1.setUser(trainerUser1);

        Trainer trainer2 = new Trainer();
        trainer2.setTrainees(new HashSet<>());
        trainer2.setSpecialization(new TrainingType());
        User trainerUser2 = new User();
        trainerUser2.setUsername("trainer2");
        trainer2.setUser(trainerUser2);

        when(traineeRepository.findByUserUsername(username)).thenReturn(Optional.of(trainee));
        when(trainerRepository.findAll()).thenReturn(Arrays.asList(trainer1, trainer2));

        Set<TrainersTraineeResponse> response = traineeService.updateTrainersTraineeList(username, trainerUsernames);

        assertNotNull(response);
        assertEquals(2, response.size());
    }
}