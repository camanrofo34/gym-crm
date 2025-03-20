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

class TrainerServiceTest {

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private TrainingTypeRepository trainingTypeRepository;

    @Mock
    private UserUtil userUtil;

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

        Optional<UserCreationResponse> response = trainerService.createTrainer(request);

        assertTrue(response.isPresent());
        assertEquals("johndoe", response.get().getUsername());
        assertEquals("password123", response.get().getPassword());
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
        trainer.setUser(user);
        trainer.setSpecialization(new TrainingType());
        trainer.setTrainees(new ArrayList<>());
        when(trainerRepository.findByUserUsername(anyString())).thenReturn(Optional.of(trainer));

        Optional<TrainerGetProfileResponse> response = trainerService.getTrainerByUsername("johndoe");

        assertTrue(response.isPresent());
    }

    @Test
    void getTrainerByUsername_NotFound() {
        when(trainerRepository.findByUserUsername(anyString())).thenReturn(Optional.empty());

        Optional<TrainerGetProfileResponse> response = trainerService.getTrainerByUsername("johndoe");

        assertFalse(response.isPresent());
    }

    @Test
    void updateTrainerProfile_Success() {
        Trainer trainer = new Trainer();
        User user = new User();
        user.setUsername("johndoe");
        trainer.setUser(user);
        trainer.setTrainees(new ArrayList<>());
        when(trainerRepository.findByUserUsername(anyString())).thenReturn(Optional.of(trainer));
        when(trainingTypeRepository.getReferenceById(anyLong())).thenReturn(new TrainingType());
        when(trainerRepository.save(any(Trainer.class))).thenReturn(trainer);

        TrainerUpdateRequest request = new TrainerUpdateRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setTrainingTypeId(1L);

        Optional<TrainerUpdateResponse> response = trainerService.updateTrainerProfile("johndoe", request);

        assertTrue(response.isPresent());
        assertEquals("John", response.get().getFirstName());
        assertEquals("Doe", response.get().getLastName());
    }

    @Test
    void updateTrainerProfile_NotFound() {
        when(trainerRepository.findByUserUsername(anyString())).thenReturn(Optional.empty());

        TrainerUpdateRequest request = new TrainerUpdateRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setTrainingTypeId(1L);

        Optional<TrainerUpdateResponse> response = trainerService.updateTrainerProfile("johndoe", request);

        assertFalse(response.isPresent());
    }

}