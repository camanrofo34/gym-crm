package gym.crm.backend.service;

import gym.crm.backend.domain.entities.Trainer;
import gym.crm.backend.domain.entities.TrainingType;
import gym.crm.backend.domain.entities.User;
import gym.crm.backend.domain.request.TrainerCreationRequest;
import gym.crm.backend.domain.request.TrainerUpdateRequest;
import gym.crm.backend.domain.response.trainer.TrainerGetProfileResponse;
import gym.crm.backend.domain.response.UserCreationResponse;
import gym.crm.backend.domain.response.trainer.TrainerUpdateResponse;
import gym.crm.backend.repository.TrainerRepository;
import gym.crm.backend.repository.TrainingTypeRepository;
import gym.crm.backend.repository.UserRepository;
import gym.crm.backend.util.UserUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TrainerServiceTest {

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private TrainingTypeRepository trainingTypeRepository;

    @Mock
    private UserUtil userUtil;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TrainerService trainerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createTrainer_Success() {
        TrainerCreationRequest request = new TrainerCreationRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setTrainingTypeId(1L);
        when(trainerRepository.findAll()).thenReturn(List.of());
        when(userUtil.generateUsername(anyString(), anyString(), anyList())).thenReturn("johndoe");
        when(userUtil.generatePassword()).thenReturn("password123");
        when(trainingTypeRepository.getReferenceById(anyLong())).thenReturn(new TrainingType());
        when(trainerRepository.save(any(Trainer.class))).thenReturn(new Trainer());
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(trainingTypeRepository.findById(anyLong())).thenReturn(Optional.of(new TrainingType()));
        UserCreationResponse response = trainerService.createTrainer(request);

        assertEquals("johndoe", response.getUsername());
        assertEquals("password123", response.getPassword());
    }

    @Test
    void createTrainer_Failure_GenerateUsername() {
        TrainerCreationRequest request = new TrainerCreationRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        when(trainerRepository.findAll()).thenReturn(List.of());
        when(userUtil.generateUsername(anyString(), anyString(), anyList())).thenReturn(null);

        assertThrows(RuntimeException.class, () -> trainerService.createTrainer(request));
    }

    @Test
    void createTrainer_Failure_GeneratePassword() {
        TrainerCreationRequest request = new TrainerCreationRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        when(trainerRepository.findAll()).thenReturn(List.of());
        when(userUtil.generateUsername(anyString(), anyString(), anyList())).thenReturn("johndoe");
        when(userUtil.generatePassword()).thenReturn(null);

        assertThrows(RuntimeException.class, () -> trainerService.createTrainer(request));
    }

    @Test
    void getTrainerByUsername_Success() {
        Trainer trainer = new Trainer();
        User user = new User();
        user.setUsername("johndoe");
        user.setFirstName("John");
        trainer.setUser(user);
        trainer.setSpecialization(new TrainingType());
        trainer.setTrainees(Collections.emptySet());
        when(trainerRepository.findByUserUsername(anyString())).thenReturn(Optional.of(trainer));

        TrainerGetProfileResponse response = trainerService.getTrainerByUsername("johndoe");

        assertEquals("John", response.getFirstName());
    }

    @Test
    void updateTrainerProfile_Success() {
        Trainer trainer = new Trainer();
        User user = new User();
        user.setUsername("johndoe");
        trainer.setUser(user);
        trainer.setTrainees(Collections.emptySet());
        when(trainerRepository.findByUserUsername(anyString())).thenReturn(Optional.of(trainer));
        when(trainingTypeRepository.getReferenceById(anyLong())).thenReturn(new TrainingType());
        when(trainerRepository.save(any(Trainer.class))).thenReturn(trainer);
        when(trainingTypeRepository.findById(anyLong())).thenReturn(Optional.of(new TrainingType()));
        TrainerUpdateRequest request = new TrainerUpdateRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setTrainingTypeId(1L);

        TrainerUpdateResponse response = trainerService.updateTrainerProfile("johndoe", request);

        assertEquals("John", response.getFirstName());
        assertEquals("Doe", response.getLastName());
    }


}